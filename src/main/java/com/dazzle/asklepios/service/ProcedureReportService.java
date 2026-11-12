package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.AldreteComponent;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Allergy;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Cancellation;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.CheckItem;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Complication;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.EquipmentItem;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Header;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Kpi;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Medication;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Monitoring;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.PatientInfo;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Performance;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.PostCare;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.PreAssessment;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Registration;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Request;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.Signature;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.StaffMember;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.TimelineStep;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.VitalReading;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.VitalsComparisonRow;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.VitalsSet;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Builds the procedure report for one {@code patient_procedure} row and everything the procedure
 * workflow (patient-service) hangs off it: staff, pre-assessment, performance, vital monitoring,
 * medications, complications, equipment and post-procedure care.
 *
 * <p>Timestamps are stored as UTC wall-clock values in {@code timestamp} columns
 * ({@code hibernate.jdbc.time_zone: UTC}), so they are read as UTC and shown in the caller's zone.
 */
@Service
@Transactional(readOnly = true)
public class ProcedureReportService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureReportService.class);

    private static final String ENTITY = "patientProcedure";
    private static final String DASH = "—";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    private static final double ALDRETE_RING_CIRCUMFERENCE = 2 * Math.PI * 34;
    private static final int ALDRETE_DISCHARGE_THRESHOLD = 9;

    private static final List<String[]> SAFETY_CHECKLIST = List.of(
            new String[]{"patient_identity_verified", "chkIdentityVerified"},
            new String[]{"consent_confirmed", "chkConsentConfirmed"},
            new String[]{"procedure_site_marked", "chkSiteMarked"},
            new String[]{"allergies_confirmed", "chkAllergiesConfirmed"},
            new String[]{"patient_premedicated", "chkPremedicated"},
            new String[]{"equipment_counting_done", "chkEquipmentCounted"}
    );

    private static final List<String[]> DISCHARGE_CRITERIA = List.of(
            new String[]{"awake_and_oriented", "dcAwake"},
            new String[]{"tolerating_oral_fluids", "dcOralFluids"},
            new String[]{"ambulating_independently", "dcAmbulating"},
            new String[]{"voided_urine", "dcVoided"},
            new String[]{"no_active_bleeding", "dcNoBleeding"},
            new String[]{"pain_score_4", "dcPainControlled"}
    );

    /** Aldrete parts in display order: column on post care, LOV code, label key. */
    private static final List<String[]> ALDRETE_PARTS = List.of(
            new String[]{"activity", "ALDRETE_ACTIVITY", "aldActivity"},
            new String[]{"respiration", "ALDRETE_RESPIR", "aldRespiration"},
            new String[]{"circulation", "ALDRETE_CIRCU", "aldCirculation"},
            new String[]{"consciousness", "ALDRETE_CONSC", "aldConsciousness"},
            new String[]{"oxygen_saturation_level", "ALDRETE_OXSAT", "aldOxygen"}
    );

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final PatientRepository patientRepository;
    private final ReportCommonService reportCommonService;
    private final ProcedureVitalsChartRenderer chartRenderer;

    public ProcedureReportService(
            JdbcTemplate jdbc,
            PatientRepository patientRepository,
            ReportCommonService reportCommonService,
            ProcedureVitalsChartRenderer chartRenderer
    ) {
        this.jdbc = jdbc;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        this.patientRepository = patientRepository;
        this.reportCommonService = reportCommonService;
        this.chartRenderer = chartRenderer;
    }

    public ProcedureReportDTO load(Long procedureRequestId, ZoneId zone, boolean arabic) {
        LOG.debug("[ProcedureReport] load start procedureRequestId={} zone={} arabic={}", procedureRequestId, zone, arabic);

        Row proc = one("""
                SELECT pp.*, p.name AS procedure_name, p.code AS procedure_code, p.category_type
                FROM patient_procedure pp
                LEFT JOIN procedure p ON p.id = pp.procedure_id
                WHERE pp.id = ?
                """, procedureRequestId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Procedure request not found with id " + procedureRequestId, ENTITY, "notfound"));

        Long patientId = proc.lng("patient_id");
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundAlertException("Patient not found with id " + patientId, "patient", "notfound"));

        Row encounter = one("""
                SELECT e.encounter_number, e.encounter_date, e.encounter_type, d.name AS department_name,
                       pr.first_name AS practitioner_first_name, pr.last_name AS practitioner_last_name
                FROM patient_encounters e
                LEFT JOIN department d ON d.id = e.department_id
                LEFT JOIN practitioner pr ON pr.id = e.practitioner_id
                WHERE e.id = ?
                """, proc.lng("encounter_id")).orElse(Row.EMPTY);

        Long id = proc.lng("id");
        Row pre = one("SELECT * FROM patient_procedure_pre_assessment WHERE patient_procedure_id = ? ORDER BY id DESC LIMIT 1", id)
                .orElse(null);
        Row perf = one("SELECT * FROM patient_procedure_performance WHERE patient_procedure_id = ? ORDER BY id DESC LIMIT 1", id)
                .orElse(null);
        Row post = one("SELECT * FROM patient_procedure_post_care WHERE patient_procedure_id = ? ORDER BY id DESC LIMIT 1", id)
                .orElse(null);
        List<Row> vitals = list("""
                SELECT * FROM patient_procedure_vital_monitoring
                WHERE patient_procedure_id = ?
                ORDER BY recorded_at, id
                """, id);

        Fmt fmt = new Fmt(zone, arabic);
        String status = proc.str("status");
        boolean completed = "COMPLETED".equals(status);
        boolean cancelled = "CANCELLED".equals(status);

        Header header = new Header(
                id,
                "PRC-" + String.format(Locale.ROOT, "%06d", id),
                departmentFacility(proc.lng("to_facility_id"), proc.lng("from_department_id")),
                departmentName(proc.lng("performing_department_id") != null
                        ? proc.lng("performing_department_id")
                        : Optional.ofNullable(proc.lng("to_department_id")).orElse(proc.lng("from_department_id"))),
                orDash(proc.str("procedure_name")),
                proc.str("procedure_code"),
                reportCommonService.formatEnum(proc.str("category_type")),
                reportCommonService.formatEnum(proc.str("procedure_level")),
                reportCommonService.formatEnum(proc.str("priority")),
                status,
                statusTone(status),
                !completed && !cancelled,
                cancelled,
                fmt.dateTime(Instant.now()),
                SecurityUtils.getCurrentUserLogin().map(reportCommonService::getDisplayUserName).orElse(null)
        );

        PatientInfo patientInfo = new PatientInfo(
                orDash(reportCommonService.getPatientDisplayName(patient)),
                orDash(patient.getMedicalRecordNumber()),
                patient.getSexAtBirth() != null ? reportCommonService.formatEnum(patient.getSexAtBirth()) : DASH,
                patient.getDateOfBirth() != null ? new SimpleDateFormat("dd/MM/yyyy").format(patient.getDateOfBirth()) : DASH,
                orDash(reportCommonService.calculateAge(patient.getDateOfBirth())),
                orDash(encounter.str("encounter_number")),
                encounter.get("encounter_date") instanceof java.sql.Date d ? DATE.format(d.toLocalDate())
                        : encounter.get("encounter_date") instanceof LocalDate ld ? DATE.format(ld) : DASH,
                orDash(reportCommonService.formatEnum(encounter.str("encounter_type"))),
                orDash(encounter.str("department_name")),
                orDash(fullName(encounter.str("practitioner_first_name"), encounter.str("practitioner_last_name")))
        );

        Request request = new Request(
                orDash(reportCommonService.getDisplayUserName(proc.str("created_by"))),
                fmt.dateTime(proc.instant("created_date")),
                orDash(departmentName(proc.lng("from_department_id"))),
                orDash(facilityName(proc.lng("to_facility_id"))),
                orDash(departmentName(proc.lng("to_department_id"))),
                fmt.dateTime(proc.instant("scheduled_date_time")),
                orDash(icd(proc.lng("indication_id"))),
                orDash(proc.str("body_part")),
                orDash(proc.str("side")),
                blankToNull(proc.str("notes")),
                blankToNull(proc.str("extra_documentation"))
        );

        Row practitioner = proc.lng("performing_practitioner_id") == null ? Row.EMPTY
                : one("SELECT first_name, last_name, specialty FROM practitioner WHERE id = ?",
                proc.lng("performing_practitioner_id")).orElse(Row.EMPTY);
        String performingPractitioner = fullName(practitioner.str("first_name"), practitioner.str("last_name"));
        String performingSpecialty = blankToNull(reportCommonService.formatEnum(practitioner.str("specialty")));

        Registration registration = new Registration(
                proc.str("consent_form") != null || proc.instant("registration_date_time") != null
                        || proc.lng("performing_department_id") != null || proc.lng("performing_practitioner_id") != null,
                orDash(proc.str("consent_form")),
                fmt.dateTime(proc.instant("registration_date_time")),
                orDash(departmentName(proc.lng("performing_department_id"))),
                orDash(performingPractitioner),
                performingSpecialty
        );

        List<StaffMember> staff = list("""
                SELECT s.responsibility, s.is_present, u.first_name, u.last_name, u.login, u.job_role
                FROM patient_procedure_staff s
                LEFT JOIN app_user u ON u.id = s.user_id
                WHERE s.patient_procedure_id = ?
                ORDER BY s.id
                """, id).stream()
                .map(r -> new StaffMember(
                        orDash(Optional.ofNullable(fullName(r.str("first_name"), r.str("last_name"))).orElse(r.str("login"))),
                        orDash(reportCommonService.formatEnum(r.str("job_role"))),
                        orDash(r.str("responsibility")),
                        r.bool("is_present")))
                .toList();

        PreAssessment preAssessment = buildPreAssessment(pre);
        Performance performance = buildPerformance(perf, proc, fmt);
        Monitoring monitoring = buildMonitoring(vitals, fmt);

        List<Medication> medications = list("""
                SELECT m.dose, m.unit, m.created_date, m.created_by, ai.name AS ingredient_name
                FROM patient_procedure_medication m
                LEFT JOIN active_ingredients ai ON ai.id = m.active_ingredient_id
                WHERE m.patient_procedure_id = ?
                ORDER BY m.id
                """, id).stream()
                .map(r -> new Medication(
                        orDash(r.str("ingredient_name")),
                        joinNonBlank(" ", decimal(r.dec("dose")), r.str("unit")),
                        fmt.dateTime(r.instant("created_date")),
                        orDash(reportCommonService.getDisplayUserName(r.str("created_by")))))
                .toList();

        List<Complication> complications = list("""
                SELECT complication_type, severity, actions_taken, created_date
                FROM patient_procedure_complication
                WHERE patient_procedure_id = ?
                ORDER BY id
                """, id).stream()
                .map(r -> new Complication(
                        orDash(r.str("complication_type")),
                        orDash(r.str("severity")),
                        severityTone(r.str("severity")),
                        blankToNull(r.str("actions_taken")),
                        fmt.dateTime(r.instant("created_date"))))
                .toList();

        PostCare postCare = buildPostCare(post, fmt);

        List<EquipmentItem> equipment = list("""
                SELECT psp.billing_item_type, psp.quantity, psp.notes,
                       COALESCE(sv.name, bm.name, dt.name, pr.name, vb.name) AS item_name
                FROM patient_procedure_service_equipment e
                JOIN patient_services_and_products psp ON psp.id = e.service_product_id
                LEFT JOIN service sv ON sv.id = psp.service_id
                LEFT JOIN brand_medication bm ON bm.id = psp.brand_medication_id
                LEFT JOIN diagnostic_test dt ON dt.id = psp.diagnostic_test_id
                LEFT JOIN procedure pr ON pr.id = psp.procedure_id
                LEFT JOIN vaccine_brands vb ON vb.id = psp.vaccine_brand_id
                WHERE e.patient_procedure_id = ?
                  AND psp.payment_status <> 'CANCELLED'
                ORDER BY psp.id
                """, id).stream()
                .map(r -> new EquipmentItem(
                        orDash(r.str("item_name")),
                        orDash(reportCommonService.formatEnum(r.str("billing_item_type"))),
                        orDash(r.str("quantity")),
                        blankToNull(r.str("notes"))))
                .toList();

        Cancellation cancellation = cancelled
                ? new Cancellation(
                fmt.dateTime(proc.instant("cancelled_date")),
                orDash(reportCommonService.getDisplayUserName(proc.str("cancelled_by"))),
                orDash(proc.str("cancellation_reason")))
                : null;

        List<TimelineStep> timeline = buildTimeline(proc, perf, post, status, fmt);
        List<Kpi> kpis = buildKpis(proc, perf, postCare, complications, staff, fmt);
        List<VitalsComparisonRow> comparison = buildVitalsComparison(pre, vitals, post);

        List<Signature> signatures = new ArrayList<>();
        signatures.add(new Signature("sigPerforming", orDash(performingPractitioner), performingSpecialty));
        if (performance.anesthesiaUsed()) {
            signatures.add(new Signature("sigAnesthesia", performance.anesthesiaAdministeredBy(), null));
        }
        String recorder = post != null ? post.str("last_modified_by") : perf != null ? perf.str("last_modified_by") : null;
        signatures.add(new Signature("sigRecordedBy", orDash(reportCommonService.getDisplayUserName(recorder)), null));

        LOG.debug("[ProcedureReport] load done procedureRequestId={} staff={} vitals={} meds={} complications={}",
                procedureRequestId, staff.size(), vitals.size(), medications.size(), complications.size());

        return new ProcedureReportDTO(
                header,
                patientInfo,
                allergies(patientId),
                request,
                registration,
                timeline,
                kpis,
                staff,
                preAssessment,
                performance,
                monitoring,
                comparison,
                medications,
                complications,
                postCare,
                equipment,
                cancellation,
                signatures
        );
    }

    private PreAssessment buildPreAssessment(Row pre) {
        if (pre == null) {
            return new PreAssessment(false, false, false, null,
                    SAFETY_CHECKLIST.stream().map(c -> new CheckItem(c[1], false)).toList(), 0,
                    new VitalsSet(DASH, DASH, DASH, DASH), DASH, DASH);
        }
        List<CheckItem> checklist = SAFETY_CHECKLIST.stream().map(c -> new CheckItem(c[1], pre.bool(c[0]))).toList();
        return new PreAssessment(
                true,
                pre.bool("fasting_required"),
                pre.bool("patient_prepared"),
                blankToNull(pre.str("special_instructions")),
                checklist,
                (int) checklist.stream().filter(CheckItem::checked).count(),
                vitalsSet(pre),
                orDash(pre.str("asa_score")),
                orDash(pre.str("airway_grade"))
        );
    }

    private Performance buildPerformance(Row perf, Row proc, Fmt fmt) {
        if (perf == null) {
            return new Performance(false, DASH, DASH, DASH, false, DASH, DASH, DASH, DASH, DASH, false,
                    DASH, null, blankToNull(proc.str("result")), null, null, null);
        }
        boolean anesthesia = perf.bool("anesthesia_used");
        String administeredBy = DASH;
        if (perf.lng("anesthesia_administered_by_id") != null) {
            Row user = one("SELECT first_name, last_name, login FROM app_user WHERE id = ?",
                    perf.lng("anesthesia_administered_by_id")).orElse(Row.EMPTY);
            administeredBy = orDash(Optional.ofNullable(fullName(user.str("first_name"), user.str("last_name")))
                    .orElse(user.str("login")));
        }
        return new Performance(
                true,
                fmt.dateTime(perf.instant("actual_start_time")),
                fmt.dateTime(perf.instant("actual_end_time")),
                fmt.duration(perf.instant("actual_start_time"), perf.instant("actual_end_time")),
                anesthesia,
                orDash(perf.str("anesthesia_type")),
                fmt.dateTime(perf.instant("anesthesia_start_time")),
                fmt.dateTime(perf.instant("anesthesia_end_time")),
                fmt.duration(perf.instant("anesthesia_start_time"), perf.instant("anesthesia_end_time")),
                administeredBy,
                perf.bool("time_out"),
                orDash(perf.str("procedure_outcome")),
                blankToNull(perf.str("observations")),
                blankToNull(proc.str("result")),
                blankToNull(perf.str("additional_notes")),
                blankToNull(perf.str("home_instruction")),
                blankToNull(perf.str("home_instruction_notes"))
        );
    }

    private Monitoring buildMonitoring(List<Row> vitals, Fmt fmt) {
        boolean multiDay = vitals.stream()
                .map(v -> v.instant("recorded_at"))
                .filter(Objects::nonNull)
                .map(i -> i.atZone(fmt.zone()).toLocalDate())
                .distinct()
                .count() > 1;
        List<VitalReading> readings = vitals.stream()
                .map(v -> new VitalReading(
                        multiDay ? fmt.dateTime(v.instant("recorded_at")) : fmt.time(v.instant("recorded_at")),
                        orDash(decimal(v.dec("heart_rate"))),
                        orDash(decimal(v.dec("oxygen_saturation"))),
                        bloodPressure(v.dec("blood_pressure_systolic"), v.dec("blood_pressure_diastolic")),
                        orDash(decimal(v.dec("respiratory_rate"))),
                        orDash(decimal(v.dec("temperature"))),
                        orDash(decimal(v.dec("etco2"))),
                        orDash(decimal(v.dec("fluids_given"))),
                        orDash(decimal(v.dec("urine_output")))))
                .toList();

        String chart = chartRenderer.render(vitals.stream()
                .map(v -> new ProcedureVitalsChartRenderer.Point(
                        v.instant("recorded_at"),
                        v.dec("heart_rate"),
                        v.dec("oxygen_saturation"),
                        v.dec("blood_pressure_systolic"),
                        v.dec("blood_pressure_diastolic"),
                        v.dec("respiratory_rate")))
                .toList(), fmt.zone());

        return new Monitoring(
                readings,
                chart,
                range(vitals, "heart_rate"),
                orDash(decimal(vitals.stream().map(v -> v.dec("oxygen_saturation")).filter(Objects::nonNull)
                        .min(BigDecimal::compareTo).orElse(null))),
                range(vitals, "blood_pressure_systolic"),
                orDash(decimal(sum(vitals, "fluids_given"))),
                orDash(decimal(sum(vitals, "urine_output")))
        );
    }

    private PostCare buildPostCare(Row post, Fmt fmt) {
        if (post == null) {
            return new PostCare(false, List.of(), null, new VitalsSet(DASH, DASH, DASH, DASH), DASH, DASH, null, "neutral",
                    null, List.of(), DASH, null, false, "0 " + ALDRETE_RING_CIRCUMFERENCE,
                    DISCHARGE_CRITERIA.stream().map(c -> new CheckItem(c[1], false)).toList(), 0, 0,
                    false, false, null, null, null, DASH, DASH);
        }

        List<String> diagnoses = list("""
                SELECT i.icd_code, i.icd_short_description
                FROM patient_procedure_post_care_diagnosis d
                JOIN icd_diagnosis i ON i.id = d.diagnosis_icd_id
                WHERE d.post_care_id = ?
                ORDER BY i.icd_code
                """, post.lng("id")).stream()
                .map(r -> icdLabel(r.str("icd_code"), r.str("icd_short_description")))
                .toList();

        Map<String, BigDecimal> scores = aldreteScores(post);
        List<AldreteComponent> aldrete = ALDRETE_PARTS.stream()
                .map(p -> {
                    String value = blankToNull(post.str(p[0]));
                    BigDecimal score = value == null ? null : scores.get(p[1] + "|" + value);
                    return new AldreteComponent(p[2], orDash(value), score == null ? null : decimal(score));
                })
                .toList();
        boolean anyAldrete = aldrete.stream().anyMatch(a -> !DASH.equals(a.value()));
        BigDecimal aldreteScore = post.dec("aldrete_score");
        Integer aldreteValue = anyAldrete && aldreteScore != null ? aldreteScore.setScale(0, RoundingMode.HALF_UP).intValue() : null;

        List<CheckItem> criteria = DISCHARGE_CRITERIA.stream().map(c -> new CheckItem(c[1], post.bool(c[0]))).toList();
        int met = (int) criteria.stream().filter(CheckItem::checked).count();

        Integer pain = parseInt(post.str("pain_score"));

        return new PostCare(
                true,
                diagnoses,
                blankToNull(post.str("patient_condition")),
                vitalsSet(post),
                orDash(post.str("pain_assessment_type")),
                orDash(post.str("pain_score")),
                pain != null ? Math.max(0, Math.min(10, pain)) : null,
                pain == null ? "neutral" : pain <= 3 ? "success" : pain <= 6 ? "warning" : "danger",
                blankToNull(post.str("pain_description")),
                aldrete,
                aldreteValue != null ? aldreteValue + " / 10" : DASH,
                aldreteValue,
                aldreteValue != null && aldreteValue >= ALDRETE_DISCHARGE_THRESHOLD,
                String.format(Locale.ROOT, "%.1f %.1f",
                        (aldreteValue == null ? 0 : Math.min(10, aldreteValue)) / 10.0 * ALDRETE_RING_CIRCUMFERENCE,
                        ALDRETE_RING_CIRCUMFERENCE),
                criteria,
                met,
                criteria.isEmpty() ? 0 : Math.round(met * 100f / criteria.size()),
                post.bool("nausea_vomiting"),
                post.bool("equipment_count_done"),
                blankToNull(post.str("count_status")),
                blankToNull(post.str("recovery_notes")),
                blankToNull(post.str("additional_observations")),
                fmt.dateTime(Optional.ofNullable(post.instant("last_modified_date")).orElse(post.instant("created_date"))),
                orDash(reportCommonService.getDisplayUserName(post.str("last_modified_by")))
        );
    }

    private List<TimelineStep> buildTimeline(Row proc, Row perf, Row post, String status, Fmt fmt) {
        record Step(String key, Instant at, String subtitle) {
        }
        List<Step> steps = new ArrayList<>();
        steps.add(new Step("stepRequested", proc.instant("created_date"), reportCommonService.getDisplayUserName(proc.str("created_by"))));
        steps.add(new Step("stepScheduled", proc.instant("scheduled_date_time"), null));
        steps.add(new Step("stepRegistered", proc.instant("registration_date_time"), blankToNull(proc.str("consent_form"))));
        if (perf != null && perf.bool("anesthesia_used")) {
            steps.add(new Step("stepAnesthesia", perf.instant("anesthesia_start_time"), blankToNull(perf.str("anesthesia_type"))));
        }
        steps.add(new Step("stepStarted", perf == null ? null : perf.instant("actual_start_time"), null));
        steps.add(new Step("stepEnded", perf == null ? null : perf.instant("actual_end_time"),
                perf == null ? null : blankToNull(perf.str("procedure_outcome"))));
        steps.add(new Step("stepRecovery", post == null ? null : post.instant("created_date"), null));

        boolean cancelled = "CANCELLED".equals(status);
        boolean completed = "COMPLETED".equals(status);

        List<TimelineStep> timeline = new ArrayList<>();
        boolean currentAssigned = completed || cancelled;
        for (Step step : steps) {
            String state;
            if (step.at() != null) {
                state = "done";
            } else if (!currentAssigned) {
                state = "current";
                currentAssigned = true;
            } else {
                state = "pending";
            }
            timeline.add(new TimelineStep(step.key(), step.at() == null ? null : fmt.dateTime(step.at()),
                    blankToNull(step.subtitle()), state, "done".equals(state) ? "success" : "neutral"));
        }

        if (cancelled) {
            timeline.add(new TimelineStep("stepCancelled", fmt.dateTime(proc.instant("cancelled_date")),
                    blankToNull(reportCommonService.getDisplayUserName(proc.str("cancelled_by"))), "done", "danger"));
        } else {
            timeline.add(new TimelineStep("stepCompleted",
                    completed ? fmt.dateTime(proc.instant("last_modified_date")) : null,
                    null, completed ? "done" : "pending", completed ? "success" : "neutral"));
        }
        return timeline;
    }

    private List<Kpi> buildKpis(Row proc, Row perf, PostCare postCare, List<Complication> complications,
                                List<StaffMember> staff, Fmt fmt) {
        List<Kpi> kpis = new ArrayList<>();
        Instant start = perf == null ? null : perf.instant("actual_start_time");
        Instant end = perf == null ? null : perf.instant("actual_end_time");
        kpis.add(new Kpi("kpiDuration", fmt.duration(start, end), fmt.timeRange(start, end), "primary"));

        if (perf != null && perf.bool("anesthesia_used")) {
            Instant aStart = perf.instant("anesthesia_start_time");
            Instant aEnd = perf.instant("anesthesia_end_time");
            kpis.add(new Kpi("kpiAnesthesia", fmt.duration(aStart, aEnd), orDash(perf.str("anesthesia_type")), "info"));
        }

        kpis.add(new Kpi("kpiRequestToStart", fmt.duration(proc.instant("created_date"), start), null, "neutral"));

        kpis.add(new Kpi("kpiAldrete", postCare.aldreteScore(), null,
                postCare.aldreteScoreValue() == null ? "neutral" : postCare.aldreteDischargeReady() ? "success" : "warning"));

        long severe = complications.stream().filter(c -> "danger".equals(c.tone())).count();
        kpis.add(new Kpi("kpiComplications", String.valueOf(complications.size()),
                severe > 0 ? String.valueOf(severe) : null,
                complications.isEmpty() ? "success" : severe > 0 ? "danger" : "warning"));

        long present = staff.stream().filter(StaffMember::present).count();
        kpis.add(new Kpi("kpiTeam", staff.isEmpty() ? DASH : present + " / " + staff.size(), null, "neutral"));
        return kpis;
    }

    private List<VitalsComparisonRow> buildVitalsComparison(Row pre, List<Row> vitals, Row post) {
        Row intra = vitals.isEmpty() ? null : vitals.get(vitals.size() - 1);
        List<VitalsComparisonRow> rows = new ArrayList<>();
        rows.add(new VitalsComparisonRow("bloodPressure",
                pre == null ? DASH : bloodPressure(pre.dec("blood_pressure_systolic"), pre.dec("blood_pressure_diastolic")),
                intra == null ? DASH : bloodPressure(intra.dec("blood_pressure_systolic"), intra.dec("blood_pressure_diastolic")),
                post == null ? DASH : bloodPressure(post.dec("blood_pressure_systolic"), post.dec("blood_pressure_diastolic")),
                trend(pre, post, "blood_pressure_systolic")));
        rows.add(comparisonRow("heartRate", "heart_rate", pre, intra, post));
        rows.add(comparisonRow("oxygenSaturation", "oxygen_saturation", pre, intra, post));
        rows.add(comparisonRow("temperature", "temperature", pre, intra, post));
        boolean any = rows.stream().anyMatch(r -> !DASH.equals(r.pre()) || !DASH.equals(r.intra()) || !DASH.equals(r.post()));
        return any ? rows : List.of();
    }

    private VitalsComparisonRow comparisonRow(String labelKey, String column, Row pre, Row intra, Row post) {
        return new VitalsComparisonRow(labelKey,
                pre == null ? DASH : orDash(decimal(pre.dec(column))),
                intra == null ? DASH : orDash(decimal(intra.dec(column))),
                post == null ? DASH : orDash(decimal(post.dec(column))),
                trend(pre, post, column));
    }

    private static String trend(Row pre, Row post, String column) {
        BigDecimal before = pre == null ? null : pre.dec(column);
        BigDecimal after = post == null ? null : post.dec(column);
        if (before == null || after == null) {
            return "none";
        }
        int cmp = after.compareTo(before);
        return cmp > 0 ? "up" : cmp < 0 ? "down" : "same";
    }

    private List<Allergy> allergies(Long patientId) {
        return list("""
                SELECT pa.allergen_type, pa.severity,
                       COALESCE(mc.name, al.name, pa.allergen_name) AS allergen
                FROM patient_allergies pa
                LEFT JOIN allergens al ON al.id = pa.allergen_id
                LEFT JOIN medication_categories_class mc ON mc.id = pa.medication_class_id
                WHERE pa.patient_id = ?
                  AND pa.status = 'ACTIVE'
                ORDER BY pa.id
                """, patientId).stream()
                .map(r -> new Allergy(
                        orDash(r.str("allergen")),
                        reportCommonService.formatEnum(r.str("allergen_type")),
                        reportCommonService.formatEnum(r.str("severity")),
                        "danger".equals(severityTone(r.str("severity")))))
                .toList();
    }

    private Map<String, BigDecimal> aldreteScores(Row post) {
        List<String> values = ALDRETE_PARTS.stream()
                .map(p -> blankToNull(post.str(p[0])))
                .filter(Objects::nonNull)
                .toList();
        if (values.isEmpty()) {
            return Map.of();
        }
        Map<String, BigDecimal> scores = new HashMap<>();
        namedJdbc.query("""
                        SELECT lov_code, lov_display_vale, score
                        FROM ap_lov_values
                        WHERE lov_code IN (:codes) AND lov_display_vale IN (:values)
                        """,
                new MapSqlParameterSource()
                        .addValue("codes", ALDRETE_PARTS.stream().map(p -> p[1]).toList())
                        .addValue("values", values),
                rs -> {
                    scores.putIfAbsent(rs.getString("lov_code") + "|" + rs.getString("lov_display_vale"), rs.getBigDecimal("score"));
                });
        return scores;
    }

    private String icd(Long id) {
        if (id == null) {
            return null;
        }
        return one("SELECT icd_code, icd_short_description FROM icd_diagnosis WHERE id = ?", id)
                .map(r -> icdLabel(r.str("icd_code"), r.str("icd_short_description")))
                .orElse(null);
    }

    /** "CODE — description", or just the description when it already carries the code. */
    static String icdLabel(String code, String description) {
        if (description != null && code != null && description.contains(code)) {
            return description.trim();
        }
        return joinNonBlank(" — ", code, description);
    }

    private String departmentName(Long id) {
        if (id == null) {
            return null;
        }
        return one("SELECT name FROM department WHERE id = ?", id).map(r -> r.str("name")).orElse(null);
    }

    private String facilityName(Long id) {
        if (id == null) {
            return null;
        }
        return one("SELECT name FROM facility WHERE id = ?", id).map(r -> r.str("name")).orElse(null);
    }

    /** Facility shown in the header: the performing facility, else the requesting department's facility. */
    private String departmentFacility(Long toFacilityId, Long fromDepartmentId) {
        String name = facilityName(toFacilityId);
        if (name != null || fromDepartmentId == null) {
            return name;
        }
        return one("SELECT f.name FROM department d JOIN facility f ON f.id = d.facility_id WHERE d.id = ?", fromDepartmentId)
                .map(r -> r.str("name")).orElse(null);
    }

    private VitalsSet vitalsSet(Row row) {
        return new VitalsSet(
                bloodPressure(row.dec("blood_pressure_systolic"), row.dec("blood_pressure_diastolic")),
                orDash(decimal(row.dec("heart_rate"))),
                orDash(decimal(row.dec("temperature"))),
                orDash(decimal(row.dec("oxygen_saturation")))
        );
    }

    private static String range(List<Row> rows, String column) {
        List<BigDecimal> values = rows.stream().map(r -> r.dec(column)).filter(Objects::nonNull).toList();
        if (values.isEmpty()) {
            return DASH;
        }
        BigDecimal min = values.stream().min(BigDecimal::compareTo).orElseThrow();
        BigDecimal max = values.stream().max(BigDecimal::compareTo).orElseThrow();
        return min.compareTo(max) == 0 ? decimal(min) : decimal(min) + " – " + decimal(max);
    }

    private static BigDecimal sum(List<Row> rows, String column) {
        return rows.stream().map(r -> r.dec(column)).filter(Objects::nonNull).reduce(BigDecimal::add).orElse(null);
    }

    static String statusTone(String status) {
        if (status == null) {
            return "neutral";
        }
        return switch (status) {
            case "COMPLETED" -> "success";
            case "CANCELLED" -> "danger";
            case "STARTED", "IN_PROGRESS", "SUBMITTED", "RETURNED_TO_BED" -> "info";
            case "AWAITING_CONSENT", "WAITING_PRE_AUTHORIZATION" -> "warning";
            default -> "neutral";
        };
    }

    static String severityTone(String severity) {
        if (severity == null) {
            return "neutral";
        }
        String s = severity.toLowerCase(Locale.ROOT);
        if (s.contains("severe") || s.contains("critical") || s.contains("high") || s.contains("life")) {
            return "danger";
        }
        if (s.contains("moderate") || s.contains("medium")) {
            return "warning";
        }
        if (s.contains("mild") || s.contains("minor") || s.contains("low")) {
            return "info";
        }
        return "neutral";
    }

    private static String bloodPressure(BigDecimal systolic, BigDecimal diastolic) {
        if (systolic == null && diastolic == null) {
            return DASH;
        }
        return orDash(decimal(systolic)) + " / " + orDash(decimal(diastolic));
    }

    static String decimal(BigDecimal value) {
        if (value == null) {
            return null;
        }
        BigDecimal stripped = value.stripTrailingZeros();
        return (stripped.scale() < 0 ? stripped.setScale(0) : stripped).toPlainString();
    }

    private static Integer parseInt(String value) {
        if (value == null) {
            return null;
        }
        try {
            return (int) Math.round(Double.parseDouble(value.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String fullName(String first, String last) {
        String name = joinNonBlank(" ", first, last);
        return DASH.equals(name) ? null : name;
    }

    private static String joinNonBlank(String separator, String... parts) {
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                values.add(part.trim());
            }
        }
        return values.isEmpty() ? DASH : String.join(separator, values);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String orDash(String value) {
        return value == null || value.isBlank() ? DASH : value;
    }

    private Optional<Row> one(String sql, Object... args) {
        List<Row> rows = list(sql, args);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    private List<Row> list(String sql, Object... args) {
        return jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> values = new LinkedHashMap<>();
            int columns = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columns; i++) {
                values.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
            }
            return new Row(values);
        }, args);
    }

    /** One result row with typed, null-safe getters. */
    record Row(Map<String, Object> values) {

        static final Row EMPTY = new Row(Map.of());

        Object get(String column) {
            return values.get(column);
        }

        String str(String column) {
            Object value = values.get(column);
            return value == null ? null : value.toString();
        }

        Long lng(String column) {
            return values.get(column) instanceof Number n ? n.longValue() : null;
        }

        BigDecimal dec(String column) {
            Object value = values.get(column);
            if (value instanceof BigDecimal b) {
                return b;
            }
            return value instanceof Number n ? BigDecimal.valueOf(n.doubleValue()) : null;
        }

        boolean bool(String column) {
            return Boolean.TRUE.equals(values.get(column));
        }

        Instant instant(String column) {
            Object value = values.get(column);
            if (value instanceof Timestamp ts) {
                return ts.toLocalDateTime().toInstant(ZoneOffset.UTC);
            }
            if (value instanceof LocalDateTime ldt) {
                return ldt.toInstant(ZoneOffset.UTC);
            }
            if (value instanceof OffsetDateTime odt) {
                return odt.toInstant();
            }
            return value instanceof Instant i ? i : null;
        }
    }

    /** Formats dates, times and durations in the caller's zone and language. */
    record Fmt(ZoneId zone, boolean arabic) {

        String dateTime(Instant instant) {
            return instant == null ? DASH : DATE_TIME.format(instant.atZone(zone));
        }

        String time(Instant instant) {
            return instant == null ? DASH : TIME.format(instant.atZone(zone));
        }

        String timeRange(Instant start, Instant end) {
            if (start == null || end == null) {
                return null;
            }
            return time(start) + " → " + time(end);
        }

        String duration(Instant start, Instant end) {
            if (start == null || end == null || end.isBefore(start)) {
                return DASH;
            }
            return formatDuration(Duration.between(start, end), arabic);
        }

        static String formatDuration(Duration duration, boolean arabic) {
            long totalMinutes = duration.toMinutes();
            long days = totalMinutes / (24 * 60);
            long hours = (totalMinutes % (24 * 60)) / 60;
            long minutes = totalMinutes % 60;
            String d = arabic ? " ي" : "d";
            String h = arabic ? " س" : "h";
            String m = arabic ? " د" : "m";
            List<String> parts = new ArrayList<>();
            if (days > 0) {
                parts.add(days + d);
            }
            if (hours > 0) {
                parts.add(hours + h);
            }
            // Minutes add little once a duration runs into days
            if ((minutes > 0 && days == 0) || parts.isEmpty()) {
                parts.add(minutes + m);
            }
            return String.join(" ", parts);
        }
    }
}
