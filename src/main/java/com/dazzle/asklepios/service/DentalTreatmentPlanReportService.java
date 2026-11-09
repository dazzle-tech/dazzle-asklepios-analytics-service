package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.dto.DentalTreatmentPlanReportView;
import com.dazzle.asklepios.service.dto.DentalTreatmentPlanReportView.ChartToothView;
import com.dazzle.asklepios.service.dto.DentalTreatmentPlanReportView.ImageView;
import com.dazzle.asklepios.service.dto.DentalTreatmentPlanReportView.LineView;
import com.dazzle.asklepios.service.dto.DentalTreatmentPlanReportView.SessionView;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class DentalTreatmentPlanReportService {

    private static final String ENTITY = "dentalTreatmentPlan";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    /** Matches setup EncounterAttachmentsService medical-attachment download roles. */
    private static final Set<String> MEDICAL_DOWNLOAD_ROLES = Set.of(
            "HIS_ADMINISTRATOR",
            "PHYSICIAN",
            "NURSE"
    );
    private static final Map<String, String> SURFACES = Map.of(
            "MESIAL", "Mesial",
            "DISTAL", "Distal",
            "BUCCAL", "Buccal",
            "LINGUAL", "Lingual",
            "OCCLUSAL", "Occlusal",
            "INCISAL", "Incisal",
            "FACIAL", "Facial",
            "WHOLE_TOOTH", "Whole Tooth"
    );
    private static final Map<String, String> PLAN_STATUS = Map.of(
            "DRAFT", "Draft",
            "PROPOSED", "Proposed",
            "ACCEPTED", "Accepted",
            "IN_PROGRESS", "In Progress",
            "ON_HOLD", "On Hold",
            "COMPLETED", "Completed",
            "CANCELLED", "Cancelled"
    );

    private final JdbcTemplate jdbc;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final ApLovValueRepository lovRepository;
    private final ReportCommonService reportCommonService;
    private final ReportPdfCommonService reportPdfCommonService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String bucket;
    private final String region;
    private final String endpoint;
    private final String accessKeyId;
    private final String secretAccessKey;

    public DentalTreatmentPlanReportService(
            JdbcTemplate jdbc,
            PatientRepository patientRepository,
            UserRepository userRepository,
            ApLovValueRepository lovRepository,
            ReportCommonService reportCommonService,
            ReportPdfCommonService reportPdfCommonService,
            @Value("${analytics.attachments.bucket:}") String bucket,
            @Value("${analytics.attachments.region:}") String region,
            @Value("${analytics.attachments.endpoint:}") String endpoint,
            @Value("${analytics.attachments.accessKeyId:}") String accessKeyId,
            @Value("${analytics.attachments.secretAccessKey:}") String secretAccessKey
    ) {
        this.jdbc = jdbc;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.lovRepository = lovRepository;
        this.reportCommonService = reportCommonService;
        this.reportPdfCommonService = reportPdfCommonService;
        this.bucket = bucket;
        this.region = region;
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
    }

    public DentalTreatmentPlanReportView load(Long planId, boolean arabic, List<Long> attachmentIds) {
        Map<String, Object> plan = jdbc.query(
                """
                SELECT id, plan_number, patient_id, origin_encounter_id, title, practitioner_id, status,
                       created_date, last_modified_date, completed_at
                FROM dental_treatment_plan
                WHERE id = ?
                """,
                rs -> rs.next() ? row(rs) : null,
                planId
        );
        if (plan == null) {
            throw new BadRequestAlertException("Treatment plan not found", ENTITY, "notfound");
        }

        Long patientId = longValue(plan.get("patient_id"));
        Long originEncounterId = longValue(plan.get("origin_encounter_id"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BadRequestAlertException("Patient not found", ENTITY, "notfound"));

        Map<String, Object> encounter = jdbc.query(
                """
                SELECT e.encounter_number, f.name AS facility_name
                FROM patient_encounters e
                LEFT JOIN facility f ON f.id = e.facility_id
                WHERE e.id = ?
                """,
                rs -> rs.next() ? row(rs) : null,
                originEncounterId
        );

        ZoneId zone = ZoneId.systemDefault();
        DentalTreatmentPlanReportView view = new DentalTreatmentPlanReportView();
        view.setLang(arabic ? "ar" : "en");
        view.setDir(arabic ? "rtl" : "ltr");
        view.setFacilityName(text(encounter == null ? null : encounter.get("facility_name")));
        view.setPlanNumber(text(plan.get("plan_number")));
        view.setTitle(blank(text(plan.get("title")), "—"));
        view.setStatus(PLAN_STATUS.getOrDefault(text(plan.get("status")), blank(text(plan.get("status")), "—")));
        view.setPatientName(blank(reportCommonService.getPatientDisplayName(patient), "—"));
        view.setMrn(blank(patient.getMedicalRecordNumber(), "—"));
        view.setDob(formatDate(toInstant(patient.getDateOfBirth()), zone, false));
        view.setAge(blank(reportCommonService.calculateAge(patient.getDateOfBirth()), "—"));
        view.setGender(labelize(patient.getSexAtBirth()));
        view.setPractitioner(practitionerName(longValue(plan.get("practitioner_id"))));
        view.setOriginEncounter(blank(text(encounter == null ? null : encounter.get("encounter_number")), "—"));
        view.setCreated(formatDate(instant(plan.get("created_date")), zone, true));
        Instant updated = instant(plan.get("last_modified_date"));
        if (updated == null) {
            updated = instant(plan.get("completed_at"));
        }
        view.setUpdated(formatDate(updated, zone, true));
        view.setGeneratedAt(reportPdfCommonService.generatedAtDisplay(null));

        List<ToothRef> teeth = loadChart(originEncounterId, view);
        List<Map<String, Object>> treatments = jdbc.queryForList(
                """
                SELECT t.id, t.treatment_session_id, s.session_number, t.visit_number, t.treatment_scope,
                       t.tooth_id, t.covered_tooth_ids, t.surface, t.cdt_code_id, c.code AS cdt_code,
                       c.description AS cdt_description, t.service_id, sv.name AS service_name,
                       t.price, t.quantity, t.discount, t.status, t.note, t.clinical_details,
                       t.execution_encounter_id, t.completed_by, t.completed_at, t.is_cancelled,
                       t.cancellation_reason, t.cancelled_date
                FROM dental_planned_treatment t
                LEFT JOIN dental_treatment_session s ON s.id = t.treatment_session_id
                LEFT JOIN cdt_code c ON c.id = t.cdt_code_id
                LEFT JOIN service sv ON sv.id = t.service_id
                WHERE t.treatment_plan_id = ?
                ORDER BY s.session_number NULLS LAST, t.id
                """,
                planId
        );

        Set<String> highlighted = highlightedTeeth(treatments, teeth);
        applyChart(view, teeth, highlighted);
        view.setSessions(sessions(treatments, teeth, zone));
        totals(view, treatments);
        view.setImages(images(patientId, attachmentIds, zone));
        return view;
    }

    private List<ToothRef> loadChart(Long encounterId, DentalTreatmentPlanReportView view) {
        if (encounterId == null) {
            view.setChartAvailable(false);
            return List.of();
        }
        Long chartId = jdbc.query(
                "SELECT id FROM dental_chart WHERE encounter_id = ? ORDER BY id DESC LIMIT 1",
                rs -> rs.next() ? rs.getLong("id") : null,
                encounterId
        );
        if (chartId == null) {
            view.setChartAvailable(false);
            return List.of();
        }
        List<ToothRef> teeth = jdbc.query(
                "SELECT id, tooth_number, missing FROM dental_chart_tooth WHERE chart_id = ?",
                (rs, rowNum) -> new ToothRef(rs.getLong("id"), rs.getString("tooth_number"), rs.getBoolean("missing")),
                chartId
        );
        teeth = new ArrayList<>(teeth);
        teeth.sort(Comparator.comparingInt(tooth -> toothIndex(tooth.number())));
        view.setChartAvailable(!teeth.isEmpty());
        if (teeth.isEmpty()) {
            view.setChartMessage("Dental chart unavailable");
        }
        return teeth;
    }

    private void applyChart(DentalTreatmentPlanReportView view, List<ToothRef> teeth, Set<String> highlighted) {
        if (!view.isChartAvailable()) {
            return;
        }
        double cutoff = teeth.size() / 2.0;
        List<ChartToothView> upper = new ArrayList<>();
        List<ChartToothView> lower = new ArrayList<>();
        for (ToothRef tooth : teeth) {
            ChartToothView cell = new ChartToothView();
            cell.setLabel(displayLabel(tooth.number()));
            cell.setHighlighted(highlighted.contains(tooth.number()));
            cell.setMissing(tooth.missing());
            cell.setImage(toothImage(tooth.number()));
            if (toothIndex(tooth.number()) <= cutoff) {
                upper.add(cell);
            } else {
                lower.add(cell);
            }
        }
        view.setUpperTeeth(upper);
        view.setLowerTeeth(lower);
    }

    private Set<String> highlightedTeeth(List<Map<String, Object>> treatments, List<ToothRef> teeth) {
        Map<Long, String> byId = new HashMap<>();
        teeth.forEach(tooth -> byId.put(tooth.id(), tooth.number()));
        java.util.HashSet<String> highlighted = new java.util.HashSet<>();
        for (Map<String, Object> row : treatments) {
            if (bool(row.get("is_cancelled"))) {
                continue;
            }
            String scope = blank(text(row.get("treatment_scope")), "SPECIFIC_TOOTH");
            if ("SPECIFIC_TOOTH".equals(scope)) {
                String number = byId.get(longValue(row.get("tooth_id")));
                if (number != null) {
                    highlighted.add(number);
                }
                continue;
            }
            List<Long> coveredIds = parseIds(text(row.get("covered_tooth_ids")));
            List<ToothRef> covered = coveredIds.isEmpty()
                    ? teethForScope(teeth, scope)
                    : coveredIds.stream()
                    .map(id -> teeth.stream().filter(tooth -> tooth.id() == id).findFirst().orElse(null))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            covered.forEach(tooth -> highlighted.add(tooth.number()));
        }
        return highlighted;
    }

    private List<SessionView> sessions(List<Map<String, Object>> treatments, List<ToothRef> teeth, ZoneId zone) {
        Map<Long, String> byId = new HashMap<>();
        teeth.forEach(tooth -> byId.put(tooth.id(), tooth.number()));
        Map<String, SessionView> groups = new LinkedHashMap<>();
        Map<String, Integer> order = new HashMap<>();
        for (Map<String, Object> row : treatments) {
            Long sessionId = longValue(row.get("treatment_session_id"));
            Integer number = row.get("session_number") == null
                    ? intValue(row.get("visit_number"))
                    : intValue(row.get("session_number"));
            String key = sessionId == null ? "visit-" + number : "session-" + sessionId;
            SessionView session = groups.computeIfAbsent(key, ignored -> {
                SessionView created = new SessionView();
                created.setTitle(number == null ? "No session" : "Session " + number);
                return created;
            });
            order.putIfAbsent(key, number == null ? Integer.MAX_VALUE : number);

            LineView line = new LineView();
            line.setArea(areaLabel(row, byId));
            line.setSurface(SURFACES.getOrDefault(text(row.get("surface")), blank(text(row.get("surface")), "—")));
            line.setCdt(cdtLabel(row));
            line.setProcedure(blank(text(row.get("service_name")), "—"));
            BigDecimal unitPrice = decimal(row.get("price"));
            BigDecimal quantity = row.get("quantity") == null
                    ? BigDecimal.ONE
                    : BigDecimal.valueOf(longValue(row.get("quantity")));
            BigDecimal discountAmount = decimal(row.get("discount"));
            BigDecimal lineNet = unitPrice.multiply(quantity).subtract(discountAmount)
                    .setScale(2, RoundingMode.HALF_UP);
            line.setPrice(money(unitPrice));
            line.setQuantity(String.valueOf(quantity.longValue()));
            line.setDiscount(money(discountAmount));
            line.setNet(money(lineNet));
            boolean cancelled = bool(row.get("is_cancelled"));
            line.setCancelled(cancelled);
            line.setStatus(cancelled ? "Cancelled" : statusLabel(text(row.get("status"))));
            line.setDetails(details(row, zone));
            session.getLines().add(line);
        }
        return groups.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> order.getOrDefault(entry.getKey(), Integer.MAX_VALUE)))
                .map(Map.Entry::getValue)
                .toList();
    }

    private void totals(DentalTreatmentPlanReportView view, List<Map<String, Object>> treatments) {
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        for (Map<String, Object> row : treatments) {
            if (bool(row.get("is_cancelled"))) {
                continue;
            }
            BigDecimal quantity = row.get("quantity") == null
                    ? BigDecimal.ONE
                    : BigDecimal.valueOf(longValue(row.get("quantity")));
            BigDecimal lineGross = decimal(row.get("price")).multiply(quantity)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineDiscount = decimal(row.get("discount")).setScale(2, RoundingMode.HALF_UP);
            gross = gross.add(lineGross);
            discount = discount.add(lineDiscount);
        }
        gross = gross.setScale(2, RoundingMode.HALF_UP);
        discount = discount.setScale(2, RoundingMode.HALF_UP);
        view.setGross(money(gross));
        view.setDiscount(money(discount));
        view.setNet(money(gross.subtract(discount).setScale(2, RoundingMode.HALF_UP)));
    }

    private List<ImageView> images(Long patientId, List<Long> attachmentIds, ZoneId zone) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return List.of();
        }
        List<ImageView> images = new ArrayList<>();
        for (Long attachmentId : attachmentIds.stream().filter(java.util.Objects::nonNull).distinct().toList()) {
            Map<String, Object> file = jdbc.query(
                    """
                    SELECT a.id, a.filename, a.mime_type, a.details, a.space_key, a.type, a.created_date,
                           a.deleted_at, e.patient_id, e.encounter_number
                    FROM encounter_attachments a
                    JOIN patient_encounters e ON e.id = a.encounter_id
                    WHERE a.id = ?
                    """,
                    rs -> rs.next() ? row(rs) : null,
                    attachmentId
            );
            if (file == null || file.get("deleted_at") != null) {
                throw new BadRequestAlertException(
                        "Selected attachment " + attachmentId + " is not available.",
                        ENTITY,
                        "attachment_unavailable"
                );
            }
            if (!patientId.equals(longValue(file.get("patient_id")))) {
                throw new BadRequestAlertException(
                        "Selected attachment " + attachmentId + " does not belong to this patient.",
                        ENTITY,
                        "attachment_forbidden"
                );
            }
            assertDownloadAllowed(text(file.get("type")), text(file.get("filename")));
            String mime = text(file.get("mime_type"));
            if (!mime.startsWith("image/")) {
                throw new BadRequestAlertException(
                        (blank(text(file.get("filename")), "A selected file")) + " cannot be printed as an image.",
                        ENTITY,
                        "attachment_not_image"
                );
            }
            byte[] bytes = readObject(text(file.get("space_key")), text(file.get("filename")));
            ImageView image = new ImageView();
            image.setName(blank(text(file.get("filename")), "Image"));
            image.setDescription(text(file.get("details")));
            image.setEncounter(blank(text(file.get("encounter_number")), "—"));
            image.setUploaded(formatDate(instant(file.get("created_date")), zone, true));
            image.setSrc("data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes));
            images.add(image);
        }
        return images;
    }

    private void assertDownloadAllowed(String type, String filename) {
        if (type == null || type.isBlank()) {
            return;
        }
        ApLovValue lov = lovRepository.findById(type).orElse(null);
        if (lov == null || !"ATAC_TYP_MED".equals(lov.getValueCode())) {
            return;
        }
        String login = SecurityUtils.getCurrentUserLogin().orElse("system");
        String role = userRepository.findByLogin(login).map(user -> user.getJobRole()).orElse("");
        if (!MEDICAL_DOWNLOAD_ROLES.contains(role)) {
            throw new BadRequestAlertException(
                    (blank(filename, "A selected file")) + " cannot be included with your current permission.",
                    ENTITY,
                    "attachment_forbidden"
            );
        }
    }

    private byte[] readObject(String key, String filename) {
        if (key == null || key.isBlank()) {
            throw new BadRequestAlertException(
                    (blank(filename, "A selected image")) + " could not be loaded.",
                    ENTITY,
                    "attachment_unreadable"
            );
        }
        try {
            if (bucket == null || bucket.isBlank() || accessKeyId == null || accessKeyId.isBlank()) {
                String url = reportPdfCommonService.publicUrl(key);
                try (java.io.InputStream input = URI.create(url).toURL().openStream()) {
                    return input.readAllBytes();
                }
            }
            try (S3Client client = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                    ))
                    .build()) {
                ResponseBytes<GetObjectResponse> response = client.getObjectAsBytes(
                        GetObjectRequest.builder().bucket(bucket).key(key).build()
                );
                return response.asByteArray();
            }
        } catch (Exception ex) {
            throw new BadRequestAlertException(
                    (blank(filename, "A selected image")) + " could not be loaded and was not included.",
                    ENTITY,
                    "attachment_unreadable"
            );
        }
    }

    private String toothImage(String toothNumber) {
        int index = toothIndex(toothNumber);
        if (index < 1 || index > 32 || isPrimary(toothNumber)) {
            return null;
        }
        String code = String.format("%02d", index);
        ClassPathResource resource = new ClassPathResource("static/dental/color/Color" + code + ".outer.svg");
        if (!resource.exists()) {
            return null;
        }
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception ex) {
            return null;
        }
    }

    private List<String> details(Map<String, Object> row, ZoneId zone) {
        List<String> lines = new ArrayList<>();
        if (!text(row.get("note")).isBlank()) {
            lines.add("Notes: " + text(row.get("note")));
        }
        JsonNode clinical = clinical(text(row.get("clinical_details")));
        String anesthesia = clinical.path("anesthesiaUsed").asText("");
        String unit = clinical.path("unit").asText("");
        String material = clinical.path("fillingMaterial").asText("");
        if (!anesthesia.isBlank()) {
            lines.add("Anesthesia Used: " + anesthesia);
        }
        if (clinical.hasNonNull("dose")) {
            lines.add("Dose: " + clinical.get("dose").asText() + (unit.isBlank() ? "" : " " + unit));
        } else if (!unit.isBlank()) {
            lines.add("Unit: " + unit);
        }
        if (!material.isBlank()) {
            lines.add("Filling Material: " + material);
        }
        Long executionEncounterId = longValue(row.get("execution_encounter_id"));
        if (executionEncounterId != null) {
            lines.add("Execution encounter: " + encounterNumber(executionEncounterId));
        }
        String completedBy = text(row.get("completed_by"));
        Instant completedAt = instant(row.get("completed_at"));
        if (!completedBy.isBlank() || completedAt != null) {
            lines.add("Completed by / at: " + blank(reportCommonService.getDisplayUserName(completedBy), "—")
                    + " / " + formatDate(completedAt, zone, true));
        }
        if (!text(row.get("cancellation_reason")).isBlank()) {
            String when = formatDate(instant(row.get("cancelled_date")), zone, true);
            lines.add("Cancellation reason: " + text(row.get("cancellation_reason"))
                    + ("—".equals(when) ? "" : " (" + when + ")"));
        }
        return lines;
    }

    private String areaLabel(Map<String, Object> row, Map<Long, String> byId) {
        String scope = blank(text(row.get("treatment_scope")), "SPECIFIC_TOOTH");
        if (!"SPECIFIC_TOOTH".equals(scope)) {
            return switch (scope) {
                case "UPPER_ARCH" -> "Upper Arch";
                case "LOWER_ARCH" -> "Lower Arch";
                case "FULL_MOUTH" -> "Full Mouth";
                default -> "Specific Teeth";
            };
        }
        String number = byId.get(longValue(row.get("tooth_id")));
        return number == null ? "—" : displayLabel(number);
    }

    private List<ToothRef> teethForScope(List<ToothRef> teeth, String scope) {
        double cutoff = teeth.size() / 2.0;
        return switch (scope) {
            case "UPPER_ARCH" -> teeth.stream().filter(tooth -> toothIndex(tooth.number()) <= cutoff).toList();
            case "LOWER_ARCH" -> teeth.stream().filter(tooth -> toothIndex(tooth.number()) > cutoff).toList();
            case "FULL_MOUTH" -> teeth;
            default -> List.of();
        };
    }

    private String encounterNumber(Long encounterId) {
        String number = jdbc.query(
                "SELECT encounter_number FROM patient_encounters WHERE id = ?",
                rs -> rs.next() ? rs.getString(1) : null,
                encounterId
        );
        return blank(number, String.valueOf(encounterId));
    }

    private String practitionerName(Long practitionerId) {
        if (practitionerId == null) {
            return "—";
        }
        String name = jdbc.query(
                "SELECT first_name, last_name FROM practitioner WHERE id = ?",
                rs -> rs.next()
                        ? (blank(rs.getString("first_name"), "") + " " + blank(rs.getString("last_name"), "")).trim()
                        : null,
                practitionerId
        );
        return blank(name, "—");
    }

    private JsonNode clinical(String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }

    private String cdtLabel(Map<String, Object> row) {
        String code = text(row.get("cdt_code"));
        String description = text(row.get("cdt_description"));
        if (code.isBlank() && description.isBlank()) {
            return "—";
        }
        if (description.isBlank()) {
            return code;
        }
        if (code.isBlank()) {
            return description;
        }
        return code + " — " + description;
    }

    private static String statusLabel(String status) {
        return switch (blank(status, "")) {
            case "COMPLETED" -> "Completed";
            case "IN_PROGRESS" -> "In Progress";
            case "CANCELLED" -> "Cancelled";
            default -> "Pending";
        };
    }

    private static int toothIndex(String toothNumber) {
        String remainder = toothNumber == null ? "" : toothNumber.replace("Tooth", "");
        if (remainder.length() == 1 && remainder.charAt(0) >= 'A' && remainder.charAt(0) <= 'T') {
            return remainder.charAt(0) - 'A' + 1;
        }
        try {
            return Integer.parseInt(remainder);
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    private static boolean isPrimary(String toothNumber) {
        String remainder = toothNumber == null ? "" : toothNumber.replace("Tooth", "");
        return remainder.length() == 1 && remainder.charAt(0) >= 'A' && remainder.charAt(0) <= 'T';
    }

    private static String displayLabel(String toothNumber) {
        return toothNumber == null ? "—" : toothNumber.replace("Tooth", "");
    }

    private static List<Long> parseIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (String part : raw.split(",")) {
            try {
                if (!part.isBlank()) {
                    ids.add(Long.parseLong(part.trim()));
                }
            } catch (NumberFormatException ignored) {
                // Skip a malformed id rather than highlighting the wrong tooth.
            }
        }
        return ids;
    }

    private static String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }

    private static String formatDate(Instant instant, ZoneId zone, boolean withTime) {
        if (instant == null) {
            return "—";
        }
        return (withTime ? DATE_TIME : DATE).format(instant.atZone(zone));
    }

    private static Instant toInstant(java.util.Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        return date.toInstant();
    }

    private static Instant instant(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        return null;
    }

    private static Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private static Integer intValue(Object value) {
        Long number = longValue(value);
        return number == null ? null : number.intValue();
    }

    private static boolean bool(Object value) {
        if (value instanceof Boolean flag) {
            return flag;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String blank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String labelize(String value) {
        String raw = blank(value, "—");
        if ("—".equals(raw)) {
            return raw;
        }
        String lower = raw.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static Map<String, Object> row(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        int columns = rs.getMetaData().getColumnCount();
        for (int index = 1; index <= columns; index++) {
            values.put(rs.getMetaData().getColumnLabel(index), rs.getObject(index));
        }
        return values;
    }

    private record ToothRef(long id, String number, boolean missing) {
    }
}
