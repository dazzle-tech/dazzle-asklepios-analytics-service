package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.ICDDiagnosis;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientProcedure;
import com.dazzle.asklepios.domain.PatientProcedurePerformance;
import com.dazzle.asklepios.domain.PatientProcedurePostCare;
import com.dazzle.asklepios.domain.PatientProcedurePreAssessment;
import com.dazzle.asklepios.domain.PatientProcedureServiceEquipment;
import com.dazzle.asklepios.domain.PatientProcedureStaff;
import com.dazzle.asklepios.domain.PatientProcedureVitalMonitoring;
import com.dazzle.asklepios.domain.PatientServiceAndProduct;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.ICDDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientAllergyRepository;
import com.dazzle.asklepios.repository.PatientProcedureComplicationRepository;
import com.dazzle.asklepios.repository.PatientProcedureMedicationRepository;
import com.dazzle.asklepios.repository.PatientProcedurePerformanceRepository;
import com.dazzle.asklepios.repository.PatientProcedurePostCareRepository;
import com.dazzle.asklepios.repository.PatientProcedurePreAssessmentRepository;
import com.dazzle.asklepios.repository.PatientProcedureRepository;
import com.dazzle.asklepios.repository.PatientProcedureServiceEquipmentRepository;
import com.dazzle.asklepios.repository.PatientProcedureStaffRepository;
import com.dazzle.asklepios.repository.PatientProcedureVitalMonitoringRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.PatientServiceAndProductRepository;
import com.dazzle.asklepios.repository.PractitionersRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.repository.ServiceItemRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.repository.VaccineBrandRepository;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Builds the procedure report for one {@code patient_procedure} row and everything the procedure
 * workflow (patient-service) hangs off it: staff, pre-assessment, performance, vital monitoring,
 * medications, complications, equipment and post-procedure care.
 *
 * <p>Every table is read through its own repository with derived queries; related names are
 * looked up by id in batches, so there are no hand-written SQL joins.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProcedureReportService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureReportService.class);

    private static final String ENTITY = "patientProcedure";
    private static final String DASH = "—";
    private static final String CANCELLED = "CANCELLED";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    private static final double ALDRETE_RING_CIRCUMFERENCE = 2 * Math.PI * 34;
    private static final int ALDRETE_DISCHARGE_THRESHOLD = 9;

    private record Check<T>(String labelKey, Predicate<T> value) {
    }

    private static final List<Check<PatientProcedurePreAssessment>> SAFETY_CHECKLIST = List.of(
            new Check<>("chkIdentityVerified", p -> isTrue(p.getPatientIdentityVerified())),
            new Check<>("chkConsentConfirmed", p -> isTrue(p.getConsentConfirmed())),
            new Check<>("chkSiteMarked", p -> isTrue(p.getProcedureSiteMarked())),
            new Check<>("chkAllergiesConfirmed", p -> isTrue(p.getAllergiesConfirmed())),
            new Check<>("chkPremedicated", p -> isTrue(p.getPatientPremedicated())),
            new Check<>("chkEquipmentCounted", p -> isTrue(p.getEquipmentCountingDone()))
    );

    private static final List<Check<PatientProcedurePostCare>> DISCHARGE_CRITERIA = List.of(
            new Check<>("dcAwake", p -> isTrue(p.getAwakeAndOriented())),
            new Check<>("dcOralFluids", p -> isTrue(p.getToleratingOralFluids())),
            new Check<>("dcAmbulating", p -> isTrue(p.getAmbulatingIndependently())),
            new Check<>("dcVoided", p -> isTrue(p.getVoidedUrine())),
            new Check<>("dcNoBleeding", p -> isTrue(p.getNoActiveBleeding())),
            new Check<>("dcPainControlled", p -> isTrue(p.getPainScore4()))
    );

    /** Aldrete parts in display order: LOV code, label key, selected value. */
    private record AldretePart(String lovCode, String labelKey, Function<PatientProcedurePostCare, String> value) {
    }

    private static final List<AldretePart> ALDRETE_PARTS = List.of(
            new AldretePart("ALDRETE_ACTIVITY", "aldActivity", PatientProcedurePostCare::getActivity),
            new AldretePart("ALDRETE_RESPIR", "aldRespiration", PatientProcedurePostCare::getRespiration),
            new AldretePart("ALDRETE_CIRCU", "aldCirculation", PatientProcedurePostCare::getCirculation),
            new AldretePart("ALDRETE_CONSC", "aldConsciousness", PatientProcedurePostCare::getConsciousness),
            new AldretePart("ALDRETE_OXSAT", "aldOxygen", PatientProcedurePostCare::getOxygenSaturationLevel)
    );

    private final PatientProcedureRepository patientProcedureRepository;
    private final PatientProcedureStaffRepository staffRepository;
    private final PatientProcedurePreAssessmentRepository preAssessmentRepository;
    private final PatientProcedurePerformanceRepository performanceRepository;
    private final PatientProcedurePostCareRepository postCareRepository;
    private final PatientProcedureMedicationRepository medicationRepository;
    private final PatientProcedureComplicationRepository complicationRepository;
    private final PatientProcedureVitalMonitoringRepository vitalMonitoringRepository;
    private final PatientProcedureServiceEquipmentRepository serviceEquipmentRepository;
    private final PatientServiceAndProductRepository serviceAndProductRepository;
    private final PatientRepository patientRepository;
    private final PatientAllergyRepository patientAllergyRepository;
    private final ProcedureRepository procedureRepository;
    private final DepartmentsRepository departmentsRepository;
    private final FacilityRepository facilityRepository;
    private final PractitionersRepository practitionersRepository;
    private final UserRepository userRepository;
    private final ICDDiagnosisRepository icdDiagnosisRepository;
    private final ActiveIngredientsRepository activeIngredientsRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final BrandMedicationRepository brandMedicationRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final VaccineBrandRepository vaccineBrandRepository;
    private final ApLovValueRepository apLovValueRepository;
    private final ReportCommonService reportCommonService;
    private final ProcedureVitalsChartRenderer chartRenderer;

    public ProcedureReportDTO load(Long procedureRequestId, ZoneId zone, boolean arabic) {
        LOG.debug("[ProcedureReport] load start procedureRequestId={} zone={} arabic={}", procedureRequestId, zone, arabic);

        PatientProcedure proc = patientProcedureRepository.findById(procedureRequestId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Procedure request not found with id " + procedureRequestId, ENTITY, "notfound"));
        Long id = proc.getId();

        Long patientId = proc.getPatient().getId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundAlertException("Patient not found with id " + patientId, "patient", "notfound"));
        Procedure setup = proc.getProcedure() == null ? null
                : procedureRepository.findById(proc.getProcedure().getId()).orElse(null);
        PatientEncounter encounter = proc.getEncounter();

        PatientProcedurePreAssessment pre = preAssessmentRepository.findFirstByPatientProcedureIdOrderByIdDesc(id).orElse(null);
        PatientProcedurePerformance perf = performanceRepository.findFirstByPatientProcedureIdOrderByIdDesc(id).orElse(null);
        PatientProcedurePostCare post = postCareRepository.findFirstByPatientProcedureIdOrderByIdDesc(id).orElse(null);
        List<PatientProcedureVitalMonitoring> vitals = vitalMonitoringRepository.findByPatientProcedureIdOrderByRecordedAtAscIdAsc(id);
        List<PatientProcedureStaff> staffRows = staffRepository.findByPatientProcedureIdOrderByIdAsc(id);

        // One batched lookup for every department and user the report mentions
        Map<Long, Department> departments = byId(departmentsRepository, Department::getId,
                proc.getFromDepartmentId(), proc.getToDepartmentId(), proc.getPerformingDepartmentId());
        List<Long> userIds = new ArrayList<>(staffRows.stream().map(PatientProcedureStaff::getUserId).toList());
        if (perf != null) {
            userIds.add(perf.getAnesthesiaAdministeredById());
        }
        Map<Long, User> users = byId(userRepository, User::getId, userIds.toArray(Long[]::new));

        Fmt fmt = new Fmt(zone, arabic);
        String status = proc.getStatus();
        boolean completed = "COMPLETED".equals(status);
        boolean cancelled = CANCELLED.equals(status);

        Long headerDepartmentId = firstNonNull(proc.getPerformingDepartmentId(), proc.getToDepartmentId(), proc.getFromDepartmentId());
        Header header = new Header(
                id,
                "PRC-" + String.format(Locale.ROOT, "%06d", id),
                headerFacility(proc.getToFacilityId(), departments.get(proc.getFromDepartmentId())),
                departmentName(departments, headerDepartmentId),
                orDash(setup == null ? null : setup.getName()),
                setup == null ? null : setup.getCode(),
                reportCommonService.formatEnum(setup == null ? null : setup.getCategoryType()),
                reportCommonService.formatEnum(proc.getProcedureLevel()),
                reportCommonService.formatEnum(proc.getPriority()),
                status,
                statusTone(status),
                !completed && !cancelled,
                cancelled,
                fmt.dateTime(Instant.now()),
                SecurityUtils.getCurrentUserLogin().map(reportCommonService::getDisplayUserName).orElse(null)
        );

        Practitioner attending = encounter == null ? null : encounter.getPractitioner();
        PatientInfo patientInfo = new PatientInfo(
                orDash(reportCommonService.getPatientDisplayName(patient)),
                orDash(patient.getMedicalRecordNumber()),
                patient.getSexAtBirth() != null ? reportCommonService.formatEnum(patient.getSexAtBirth()) : DASH,
                patient.getDateOfBirth() != null ? new SimpleDateFormat("dd/MM/yyyy").format(patient.getDateOfBirth()) : DASH,
                orDash(reportCommonService.calculateAge(patient.getDateOfBirth())),
                orDash(encounter == null ? null : encounter.getEncounterNumber()),
                encounter == null || encounter.getEncounterDate() == null ? DASH : DATE.format(encounter.getEncounterDate()),
                orDash(reportCommonService.formatEnum(encounter == null ? null : encounter.getEncounterType())),
                orDash(encounter == null || encounter.getDepartment() == null ? null : encounter.getDepartment().getName()),
                orDash(attending == null ? null : fullName(attending.getFirstName(), attending.getLastName()))
        );

        Request request = new Request(
                orDash(reportCommonService.getDisplayUserName(proc.getCreatedBy())),
                fmt.dateTime(proc.getCreatedDate()),
                orDash(departmentName(departments, proc.getFromDepartmentId())),
                orDash(facilityName(proc.getToFacilityId())),
                orDash(departmentName(departments, proc.getToDepartmentId())),
                fmt.dateTime(proc.getScheduledDateTime()),
                orDash(proc.getIndicationId() == null ? null
                        : icdDiagnosisRepository.findById(proc.getIndicationId()).map(ProcedureReportService::icdLabel).orElse(null)),
                orDash(proc.getBodyPart()),
                orDash(proc.getSide()),
                blankToNull(proc.getNotes()),
                blankToNull(proc.getExtraDocumentation())
        );

        Practitioner performer = proc.getPerformingPractitionerId() == null ? null
                : practitionersRepository.findById(proc.getPerformingPractitionerId()).orElse(null);
        String performingPractitioner = performer == null ? null : fullName(performer.getFirstName(), performer.getLastName());
        String performingSpecialty = performer == null ? null : blankToNull(reportCommonService.formatEnum(performer.getSpecialty()));

        Registration registration = new Registration(
                proc.getConsentForm() != null || proc.getRegistrationDateTime() != null
                        || proc.getPerformingDepartmentId() != null || proc.getPerformingPractitionerId() != null,
                orDash(proc.getConsentForm()),
                fmt.dateTime(proc.getRegistrationDateTime()),
                orDash(departmentName(departments, proc.getPerformingDepartmentId())),
                orDash(performingPractitioner),
                performingSpecialty
        );

        List<StaffMember> staff = staffRows.stream()
                .map(s -> {
                    User user = users.get(s.getUserId());
                    return new StaffMember(
                            orDash(userName(user)),
                            orDash(user == null ? null : reportCommonService.formatEnum(user.getJobRole())),
                            orDash(s.getResponsibility()),
                            isTrue(s.getIsPresent()));
                })
                .toList();

        PreAssessment preAssessment = buildPreAssessment(pre);
        Performance performance = buildPerformance(perf, proc, users, fmt);
        Monitoring monitoring = buildMonitoring(vitals, fmt);
        List<Medication> medications = buildMedications(id, fmt);
        List<Complication> complications = complicationRepository.findByPatientProcedureIdOrderByIdAsc(id).stream()
                .map(c -> new Complication(
                        orDash(c.getComplicationType()),
                        orDash(c.getSeverity()),
                        severityTone(c.getSeverity()),
                        blankToNull(c.getActionsTaken()),
                        fmt.dateTime(c.getCreatedDate())))
                .toList();
        PostCare postCare = buildPostCare(post, fmt);
        List<EquipmentItem> equipment = buildEquipment(id);

        Cancellation cancellation = cancelled
                ? new Cancellation(
                fmt.dateTime(proc.getCancelledDate()),
                orDash(reportCommonService.getDisplayUserName(proc.getCancelledBy())),
                orDash(proc.getCancellationReason()))
                : null;

        List<TimelineStep> timeline = buildTimeline(proc, perf, post, status, fmt);
        List<Kpi> kpis = buildKpis(proc, perf, postCare, complications, staff, fmt);
        List<VitalsComparisonRow> comparison = buildVitalsComparison(pre, vitals, post);

        List<Signature> signatures = new ArrayList<>();
        signatures.add(new Signature("sigPerforming", orDash(performingPractitioner), performingSpecialty));
        if (performance.anesthesiaUsed()) {
            signatures.add(new Signature("sigAnesthesia", performance.anesthesiaAdministeredBy(), null));
        }
        String recorder = post != null ? post.getLastModifiedBy() : perf != null ? perf.getLastModifiedBy() : null;
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

    private PreAssessment buildPreAssessment(PatientProcedurePreAssessment pre) {
        if (pre == null) {
            return new PreAssessment(false, false, false, null,
                    SAFETY_CHECKLIST.stream().map(c -> new CheckItem(c.labelKey(), false)).toList(), 0,
                    new VitalsSet(DASH, DASH, DASH, DASH), DASH, DASH);
        }
        List<CheckItem> checklist = SAFETY_CHECKLIST.stream().map(c -> new CheckItem(c.labelKey(), c.value().test(pre))).toList();
        return new PreAssessment(
                true,
                isTrue(pre.getFastingRequired()),
                isTrue(pre.getPatientPrepared()),
                blankToNull(pre.getSpecialInstructions()),
                checklist,
                (int) checklist.stream().filter(CheckItem::checked).count(),
                vitalsSet(pre.getBloodPressureSystolic(), pre.getBloodPressureDiastolic(), pre.getHeartRate(),
                        pre.getTemperature(), pre.getOxygenSaturation()),
                orDash(pre.getAsaScore()),
                orDash(pre.getAirwayGrade())
        );
    }

    private Performance buildPerformance(PatientProcedurePerformance perf, PatientProcedure proc, Map<Long, User> users, Fmt fmt) {
        if (perf == null) {
            return new Performance(false, DASH, DASH, DASH, false, DASH, DASH, DASH, DASH, DASH, false,
                    DASH, null, blankToNull(proc.getResult()), null, null, null);
        }
        return new Performance(
                true,
                fmt.dateTime(perf.getActualStartTime()),
                fmt.dateTime(perf.getActualEndTime()),
                fmt.duration(perf.getActualStartTime(), perf.getActualEndTime()),
                isTrue(perf.getAnesthesiaUsed()),
                orDash(perf.getAnesthesiaType()),
                fmt.dateTime(perf.getAnesthesiaStartTime()),
                fmt.dateTime(perf.getAnesthesiaEndTime()),
                fmt.duration(perf.getAnesthesiaStartTime(), perf.getAnesthesiaEndTime()),
                orDash(userName(users.get(perf.getAnesthesiaAdministeredById()))),
                isTrue(perf.getTimeOut()),
                orDash(perf.getProcedureOutcome()),
                blankToNull(perf.getObservations()),
                blankToNull(proc.getResult()),
                blankToNull(perf.getAdditionalNotes()),
                blankToNull(perf.getHomeInstruction()),
                blankToNull(perf.getHomeInstructionNotes())
        );
    }

    private Monitoring buildMonitoring(List<PatientProcedureVitalMonitoring> vitals, Fmt fmt) {
        boolean multiDay = vitals.stream()
                .map(PatientProcedureVitalMonitoring::getRecordedAt)
                .filter(Objects::nonNull)
                .map(i -> i.atZone(fmt.zone()).toLocalDate())
                .distinct()
                .count() > 1;
        List<VitalReading> readings = vitals.stream()
                .map(v -> new VitalReading(
                        multiDay ? fmt.dateTime(v.getRecordedAt()) : fmt.time(v.getRecordedAt()),
                        orDash(decimal(v.getHeartRate())),
                        orDash(decimal(v.getOxygenSaturation())),
                        bloodPressure(v.getBloodPressureSystolic(), v.getBloodPressureDiastolic()),
                        orDash(decimal(v.getRespiratoryRate())),
                        orDash(decimal(v.getTemperature())),
                        orDash(decimal(v.getEtco2())),
                        orDash(decimal(v.getFluidsGiven())),
                        orDash(decimal(v.getUrineOutput()))))
                .toList();

        String chart = chartRenderer.render(vitals.stream()
                .map(v -> new ProcedureVitalsChartRenderer.Point(
                        v.getRecordedAt(),
                        v.getHeartRate(),
                        v.getOxygenSaturation(),
                        v.getBloodPressureSystolic(),
                        v.getBloodPressureDiastolic(),
                        v.getRespiratoryRate()))
                .toList(), fmt.zone());

        return new Monitoring(
                readings,
                chart,
                range(vitals, PatientProcedureVitalMonitoring::getHeartRate),
                orDash(decimal(vitals.stream().map(PatientProcedureVitalMonitoring::getOxygenSaturation)
                        .filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(null))),
                range(vitals, PatientProcedureVitalMonitoring::getBloodPressureSystolic),
                orDash(decimal(sum(vitals, PatientProcedureVitalMonitoring::getFluidsGiven))),
                orDash(decimal(sum(vitals, PatientProcedureVitalMonitoring::getUrineOutput)))
        );
    }

    private List<Medication> buildMedications(Long procedureRequestId, Fmt fmt) {
        var rows = medicationRepository.findByPatientProcedureIdOrderByIdAsc(procedureRequestId);
        Map<Long, String> ingredients = names(activeIngredientsRepository,
                rows.stream().map(m -> m.getActiveIngredientId()).toList(), a -> a.getId(), a -> a.getName());
        return rows.stream()
                .map(m -> new Medication(
                        orDash(ingredients.get(m.getActiveIngredientId())),
                        joinNonBlank(" ", decimal(m.getDose()), m.getUnit()),
                        fmt.dateTime(m.getCreatedDate()),
                        orDash(reportCommonService.getDisplayUserName(m.getCreatedBy()))))
                .toList();
    }

    private List<EquipmentItem> buildEquipment(Long procedureRequestId) {
        List<Long> itemIds = serviceEquipmentRepository.findByPatientProcedureIdOrderByIdAsc(procedureRequestId).stream()
                .map(PatientProcedureServiceEquipment::getServiceProductId)
                .toList();
        if (itemIds.isEmpty()) {
            return List.of();
        }
        List<PatientServiceAndProduct> items = serviceAndProductRepository.findAllById(itemIds).stream()
                .filter(i -> !CANCELLED.equals(i.getPaymentStatus()))
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();

        // The item name lives in a different setup table per item type
        Map<Long, String> services = names(serviceItemRepository, items.stream().map(PatientServiceAndProduct::getServiceId).toList(),
                s -> s.getId(), s -> s.getName());
        Map<Long, String> brands = names(brandMedicationRepository, items.stream().map(PatientServiceAndProduct::getBrandMedicationId).toList(),
                b -> b.getId(), b -> b.getName());
        Map<Long, String> tests = names(diagnosticTestRepository, items.stream().map(PatientServiceAndProduct::getDiagnosticTestId).toList(),
                t -> t.getId(), t -> t.getName());
        Map<Long, String> procedures = names(procedureRepository, items.stream().map(PatientServiceAndProduct::getProcedureId).toList(),
                p -> p.getId(), p -> p.getName());
        Map<Long, String> vaccines = names(vaccineBrandRepository, items.stream().map(PatientServiceAndProduct::getVaccineBrandId).toList(),
                v -> v.getId(), v -> v.getName());

        return items.stream()
                .map(i -> new EquipmentItem(
                        orDash(firstNonNull(services.get(i.getServiceId()), brands.get(i.getBrandMedicationId()),
                                tests.get(i.getDiagnosticTestId()), procedures.get(i.getProcedureId()),
                                vaccines.get(i.getVaccineBrandId()))),
                        orDash(reportCommonService.formatEnum(i.getBillingItemType())),
                        orDash(i.getQuantity() == null ? null : String.valueOf(i.getQuantity())),
                        blankToNull(i.getNotes())))
                .toList();
    }

    private PostCare buildPostCare(PatientProcedurePostCare post, Fmt fmt) {
        if (post == null) {
            return new PostCare(false, List.of(), null, new VitalsSet(DASH, DASH, DASH, DASH), DASH, DASH, null, "neutral",
                    null, List.of(), DASH, null, false, "0 " + ALDRETE_RING_CIRCUMFERENCE,
                    DISCHARGE_CRITERIA.stream().map(c -> new CheckItem(c.labelKey(), false)).toList(), 0, 0,
                    false, false, null, null, null, DASH, DASH);
        }

        List<String> diagnoses = icdDiagnosisRepository.findAllById(post.getDiagnosisIcdIds()).stream()
                .sorted((a, b) -> String.valueOf(a.getIcdCode()).compareTo(String.valueOf(b.getIcdCode())))
                .map(ProcedureReportService::icdLabel)
                .toList();

        Map<String, BigDecimal> scores = aldreteScores(post);
        List<AldreteComponent> aldrete = ALDRETE_PARTS.stream()
                .map(p -> {
                    String value = blankToNull(p.value().apply(post));
                    BigDecimal score = value == null ? null : scores.get(p.lovCode() + "|" + value);
                    return new AldreteComponent(p.labelKey(), orDash(value), score == null ? null : decimal(score));
                })
                .toList();
        boolean anyAldrete = aldrete.stream().anyMatch(a -> !DASH.equals(a.value()));
        BigDecimal aldreteScore = post.getAldreteScore();
        Integer aldreteValue = anyAldrete && aldreteScore != null ? aldreteScore.setScale(0, RoundingMode.HALF_UP).intValue() : null;

        List<CheckItem> criteria = DISCHARGE_CRITERIA.stream().map(c -> new CheckItem(c.labelKey(), c.value().test(post))).toList();
        int met = (int) criteria.stream().filter(CheckItem::checked).count();

        Integer pain = parseInt(post.getPainScore());

        return new PostCare(
                true,
                diagnoses,
                blankToNull(post.getPatientCondition()),
                vitalsSet(post.getBloodPressureSystolic(), post.getBloodPressureDiastolic(), post.getHeartRate(),
                        post.getTemperature(), post.getOxygenSaturation()),
                orDash(post.getPainAssessmentType()),
                orDash(post.getPainScore()),
                pain != null ? Math.max(0, Math.min(10, pain)) : null,
                pain == null ? "neutral" : pain <= 3 ? "success" : pain <= 6 ? "warning" : "danger",
                blankToNull(post.getPainDescription()),
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
                isTrue(post.getNauseaVomiting()),
                isTrue(post.getEquipmentCountDone()),
                blankToNull(post.getCountStatus()),
                blankToNull(post.getRecoveryNotes()),
                blankToNull(post.getAdditionalObservations()),
                fmt.dateTime(Optional.ofNullable(post.getLastModifiedDate()).orElse(post.getCreatedDate())),
                orDash(reportCommonService.getDisplayUserName(post.getLastModifiedBy()))
        );
    }

    private List<TimelineStep> buildTimeline(PatientProcedure proc, PatientProcedurePerformance perf,
                                             PatientProcedurePostCare post, String status, Fmt fmt) {
        record Step(String key, Instant at, String subtitle) {
        }
        List<Step> steps = new ArrayList<>();
        steps.add(new Step("stepRequested", proc.getCreatedDate(), reportCommonService.getDisplayUserName(proc.getCreatedBy())));
        steps.add(new Step("stepScheduled", proc.getScheduledDateTime(), null));
        steps.add(new Step("stepRegistered", proc.getRegistrationDateTime(), blankToNull(proc.getConsentForm())));
        if (perf != null && isTrue(perf.getAnesthesiaUsed())) {
            steps.add(new Step("stepAnesthesia", perf.getAnesthesiaStartTime(), blankToNull(perf.getAnesthesiaType())));
        }
        steps.add(new Step("stepStarted", perf == null ? null : perf.getActualStartTime(), null));
        steps.add(new Step("stepEnded", perf == null ? null : perf.getActualEndTime(),
                perf == null ? null : blankToNull(perf.getProcedureOutcome())));
        steps.add(new Step("stepRecovery", post == null ? null : post.getCreatedDate(), null));

        boolean cancelled = CANCELLED.equals(status);
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
            timeline.add(new TimelineStep("stepCancelled", fmt.dateTime(proc.getCancelledDate()),
                    blankToNull(reportCommonService.getDisplayUserName(proc.getCancelledBy())), "done", "danger"));
        } else {
            timeline.add(new TimelineStep("stepCompleted",
                    completed ? fmt.dateTime(proc.getLastModifiedDate()) : null,
                    null, completed ? "done" : "pending", completed ? "success" : "neutral"));
        }
        return timeline;
    }

    private List<Kpi> buildKpis(PatientProcedure proc, PatientProcedurePerformance perf, PostCare postCare,
                                List<Complication> complications, List<StaffMember> staff, Fmt fmt) {
        List<Kpi> kpis = new ArrayList<>();
        Instant start = perf == null ? null : perf.getActualStartTime();
        Instant end = perf == null ? null : perf.getActualEndTime();
        kpis.add(new Kpi("kpiDuration", fmt.duration(start, end), fmt.timeRange(start, end), "primary"));

        if (perf != null && isTrue(perf.getAnesthesiaUsed())) {
            kpis.add(new Kpi("kpiAnesthesia", fmt.duration(perf.getAnesthesiaStartTime(), perf.getAnesthesiaEndTime()),
                    orDash(perf.getAnesthesiaType()), "info"));
        }

        kpis.add(new Kpi("kpiRequestToStart", fmt.duration(proc.getCreatedDate(), start), null, "neutral"));

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

    /** One vitals reading in a phase; the same five measures exist on pre, intra and post records. */
    private record Phase(BigDecimal systolic, BigDecimal diastolic, BigDecimal heartRate,
                         BigDecimal oxygenSaturation, BigDecimal temperature) {
    }

    private List<VitalsComparisonRow> buildVitalsComparison(PatientProcedurePreAssessment pre,
                                                            List<PatientProcedureVitalMonitoring> vitals,
                                                            PatientProcedurePostCare post) {
        Phase before = pre == null ? null : new Phase(pre.getBloodPressureSystolic(), pre.getBloodPressureDiastolic(),
                pre.getHeartRate(), pre.getOxygenSaturation(), pre.getTemperature());
        PatientProcedureVitalMonitoring last = vitals.isEmpty() ? null : vitals.get(vitals.size() - 1);
        Phase intra = last == null ? null : new Phase(last.getBloodPressureSystolic(), last.getBloodPressureDiastolic(),
                last.getHeartRate(), last.getOxygenSaturation(), last.getTemperature());
        Phase after = post == null ? null : new Phase(post.getBloodPressureSystolic(), post.getBloodPressureDiastolic(),
                post.getHeartRate(), post.getOxygenSaturation(), post.getTemperature());

        List<VitalsComparisonRow> rows = new ArrayList<>();
        rows.add(new VitalsComparisonRow("bloodPressure",
                before == null ? DASH : bloodPressure(before.systolic(), before.diastolic()),
                intra == null ? DASH : bloodPressure(intra.systolic(), intra.diastolic()),
                after == null ? DASH : bloodPressure(after.systolic(), after.diastolic()),
                trend(before, after, Phase::systolic)));
        rows.add(comparisonRow("heartRate", Phase::heartRate, before, intra, after));
        rows.add(comparisonRow("oxygenSaturation", Phase::oxygenSaturation, before, intra, after));
        rows.add(comparisonRow("temperature", Phase::temperature, before, intra, after));
        boolean any = rows.stream().anyMatch(r -> !DASH.equals(r.pre()) || !DASH.equals(r.intra()) || !DASH.equals(r.post()));
        return any ? rows : List.of();
    }

    private static VitalsComparisonRow comparisonRow(String labelKey, Function<Phase, BigDecimal> measure,
                                                     Phase before, Phase intra, Phase after) {
        return new VitalsComparisonRow(labelKey,
                before == null ? DASH : orDash(decimal(measure.apply(before))),
                intra == null ? DASH : orDash(decimal(measure.apply(intra))),
                after == null ? DASH : orDash(decimal(measure.apply(after))),
                trend(before, after, measure));
    }

    private static String trend(Phase before, Phase after, Function<Phase, BigDecimal> measure) {
        BigDecimal from = before == null ? null : measure.apply(before);
        BigDecimal to = after == null ? null : measure.apply(after);
        if (from == null || to == null) {
            return "none";
        }
        int cmp = to.compareTo(from);
        return cmp > 0 ? "up" : cmp < 0 ? "down" : "same";
    }

    private List<Allergy> allergies(Long patientId) {
        return patientAllergyRepository.findAllByPatientIdAndStatusOrderByIdAsc(patientId, "ACTIVE").stream()
                .map(a -> new Allergy(
                        orDash(allergenName(a)),
                        reportCommonService.formatEnum(a.getAllergenType()),
                        reportCommonService.formatEnum(a.getSeverity()),
                        "danger".equals(severityTone(a.getSeverity()))))
                .toList();
    }

    private static String allergenName(PatientAllergies allergy) {
        if (allergy.getMedicationClass() != null) {
            return allergy.getMedicationClass().getName();
        }
        if (allergy.getAllergen() != null) {
            return allergy.getAllergen().getName();
        }
        return allergy.getAllergenName();
    }

    private Map<String, BigDecimal> aldreteScores(PatientProcedurePostCare post) {
        List<String> values = ALDRETE_PARTS.stream()
                .map(p -> blankToNull(p.value().apply(post)))
                .filter(Objects::nonNull)
                .toList();
        if (values.isEmpty()) {
            return Map.of();
        }
        Map<String, BigDecimal> scores = new HashMap<>();
        for (ApLovValue lov : apLovValueRepository.findByLovCodeInAndLovDisplayValeIn(
                ALDRETE_PARTS.stream().map(AldretePart::lovCode).toList(), values)) {
            scores.putIfAbsent(lov.getLovCode() + "|" + lov.getLovDisplayVale(), lov.getScore());
        }
        return scores;
    }

    private String facilityName(Long id) {
        return id == null ? null : facilityRepository.findById(id).map(f -> f.getName()).orElse(null);
    }

    /** Facility shown in the header: the performing facility, else the requesting department's facility. */
    private String headerFacility(Long toFacilityId, Department fromDepartment) {
        String name = facilityName(toFacilityId);
        if (name != null || fromDepartment == null || fromDepartment.getFacility() == null) {
            return name;
        }
        return fromDepartment.getFacility().getName();
    }

    private static String departmentName(Map<Long, Department> departments, Long id) {
        Department department = id == null ? null : departments.get(id);
        return department == null ? null : department.getName();
    }

    /** Loads the rows for the given ids (nulls and duplicates ignored) in one {@code findAllById} call. */
    private static <E> Map<Long, E> byId(JpaRepository<E, Long> repository, Function<E, Long> idOf, Long... ids) {
        return byIds(repository, idOf, java.util.Arrays.asList(ids));
    }

    private static <E> Map<Long, E> byIds(JpaRepository<E, Long> repository, Function<E, Long> idOf, Collection<Long> ids) {
        List<Long> wanted = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (wanted.isEmpty()) {
            return Map.of();
        }
        return repository.findAllById(wanted).stream().collect(Collectors.toMap(idOf, Function.identity(), (a, b) -> a));
    }

    private static <E> Map<Long, String> names(JpaRepository<E, Long> repository, Collection<Long> ids,
                                               Function<E, Long> idOf, Function<E, String> nameOf) {
        Map<Long, String> names = new HashMap<>();
        byIds(repository, idOf, ids).forEach((id, entity) -> names.put(id, nameOf.apply(entity)));
        return names;
    }

    private static String userName(User user) {
        if (user == null) {
            return null;
        }
        return Optional.ofNullable(fullName(user.getFirstName(), user.getLastName())).orElse(user.getLogin());
    }

    private static VitalsSet vitalsSet(BigDecimal systolic, BigDecimal diastolic, BigDecimal heartRate,
                                       BigDecimal temperature, BigDecimal oxygenSaturation) {
        return new VitalsSet(
                bloodPressure(systolic, diastolic),
                orDash(decimal(heartRate)),
                orDash(decimal(temperature)),
                orDash(decimal(oxygenSaturation))
        );
    }

    private static <T> String range(List<T> rows, Function<T, BigDecimal> measure) {
        List<BigDecimal> values = rows.stream().map(measure).filter(Objects::nonNull).toList();
        if (values.isEmpty()) {
            return DASH;
        }
        BigDecimal min = values.stream().min(BigDecimal::compareTo).orElseThrow();
        BigDecimal max = values.stream().max(BigDecimal::compareTo).orElseThrow();
        return min.compareTo(max) == 0 ? decimal(min) : decimal(min) + " – " + decimal(max);
    }

    private static <T> BigDecimal sum(List<T> rows, Function<T, BigDecimal> measure) {
        return rows.stream().map(measure).filter(Objects::nonNull).reduce(BigDecimal::add).orElse(null);
    }

    static String statusTone(String status) {
        if (status == null) {
            return "neutral";
        }
        return switch (status) {
            case "COMPLETED" -> "success";
            case CANCELLED -> "danger";
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

    private static String icdLabel(ICDDiagnosis icd) {
        return icdLabel(icd.getIcdCode(), icd.getIcdShortDescription());
    }

    /** "CODE — description", or just the description when it already carries the code. */
    static String icdLabel(String code, String description) {
        if (description != null && code != null && description.contains(code)) {
            return description.trim();
        }
        return joinNonBlank(" — ", code, description);
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

    private static boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
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
