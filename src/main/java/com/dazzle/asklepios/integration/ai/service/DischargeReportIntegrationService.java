package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.Consultation;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestReport;
import com.dazzle.asklepios.domain.DiagnosticOrderTestResult;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientDiagnosis;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientProcedure;
import com.dazzle.asklepios.domain.ProgressNote;
import com.dazzle.asklepios.domain.UrgentCareMedicationOrder;
import com.dazzle.asklepios.domain.enumeration.ConsultationStatus;
import com.dazzle.asklepios.domain.enumeration.DiagnosisType;
import com.dazzle.asklepios.domain.enumeration.MedicationOrderStatus;
import com.dazzle.asklepios.domain.enumeration.PatientAllergyStatus;
import com.dazzle.asklepios.domain.enumeration.ProcStatus;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.integration.ai.client.DischargeReportClient;
import com.dazzle.asklepios.integration.ai.client.dto.discharge.ClinicalDocumentationDTO;
import com.dazzle.asklepios.integration.ai.client.dto.discharge.DischargeReportRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.discharge.DischargeReportResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.discharge.PatientRecordDTO;
import com.dazzle.asklepios.integration.ai.client.dto.discharge.ReportTemplateDTO;
import com.dazzle.asklepios.repository.ConsultationRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestReportRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestResultRepository;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientAllergiesRepository;
import com.dazzle.asklepios.repository.PatientDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientProcedureRepository;
import com.dazzle.asklepios.repository.ProgressNoteRepository;
import com.dazzle.asklepios.repository.UrgentCareMedicationOrderRepository;
import com.dazzle.asklepios.service.LovLookupService;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DischargeReportIntegrationService {

    private static final Logger LOG = LoggerFactory.getLogger(DischargeReportIntegrationService.class);

    private final DischargeReportClient dischargeReportClient;
    private final PatientEncounterRepository patientEncounterRepository;
    private final PatientDiagnosisRepository patientDiagnosisRepository;
    private final PatientAllergiesRepository patientAllergiesRepository;
    private final UrgentCareMedicationOrderRepository urgentCareMedicationOrderRepository;
    private final ProgressNoteRepository progressNoteRepository;
    private final PatientProcedureRepository patientProcedureRepository;
    private final ConsultationRepository consultationRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticOrderTestResultRepository diagnosticOrderTestResultRepository;
    private final DiagnosticOrderTestReportRepository diagnosticOrderTestReportRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final DiagnosticTestProfileRepository diagnosticTestProfileRepository;
    private final LovLookupService lovLookupService;

    public DischargeReportResponseDTO generateReport(Long encounterId) {
        LOG.debug("[DISCHARGE_REPORT] generating report for encounterId={}", encounterId);

        PatientEncounter encounter = patientEncounterRepository.findById(encounterId)
                .orElseThrow(() -> new NotFoundAlertException("Encounter not found with id " + encounterId, "encounter", "notfound"));
        Patient patient = encounter.getPatient();

        DischargeReportRequestDTO request = new DischargeReportRequestDTO(
                buildPatientRecord(patient, encounter),
                buildClinicalDocumentation(encounter),
                buildReportTemplate(),
                "template",
                true,
                true
        );
        LOG.debug("request={}", request);
        return dischargeReportClient.generateReport(request);
    }

    private ReportTemplateDTO buildReportTemplate() {
        return new ReportTemplateDTO(
                "Standard Discharge Summary",
                List.of(
                        "Chief Complaint",
                        "History of Present Illness",
                        "Past Medical History",
                        "Allergies",
                        "Hospital Course",
                        "Procedures Performed",
                        "Laboratory and Imaging Findings",
                        "Discharge Diagnosis",
                        "Discharge Medications",
                        "Discharge Instructions and Follow-Up"
                ),
                List.of(
                        "admission_date",
                        "discharge_date",
                        "primary_diagnosis",
                        "discharge_medications"
                ),
                "standard"
        );
    }

    private PatientRecordDTO buildPatientRecord(Patient patient, PatientEncounter encounter) {
        List<PatientDiagnosis> encounterDiagnoses = patientDiagnosisRepository.findByEncounterIdOrderByCreatedDateAsc(encounter.getId());

        String primaryDiagnosis = encounterDiagnoses.stream()
                .filter(d -> d.getType() == DiagnosisType.PRIMARY && d.getDiagnosis() != null)
                .map(d -> d.getDiagnosis().getIcdShortDescription())
                .findFirst()
                .orElse("Unknown");

        List<PatientDiagnosis> allPatientDiagnoses = patientDiagnosisRepository.findByPatientIdOrderByCreatedDateAsc(patient.getId());

        List<String> secondaryDiagnoses = allPatientDiagnoses.stream()
                .filter(d -> d.getDiagnosis() != null)
                .filter(d -> !(d.getType() == DiagnosisType.PRIMARY && d.getEncounterId().equals(encounter.getId())))
                .map(d -> d.getDiagnosis().getIcdShortDescription())
                .collect(Collectors.toList());

        List<PatientAllergies> allergies = patientAllergiesRepository
                .findByEncounterIdAndStatusOrderByCreatedDateAsc(encounter.getId(), PatientAllergyStatus.ACTIVE);
        List<String> allergyNames = allergies.stream()
                .map(this::resolveAllergyName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toList());

        List<UrgentCareMedicationOrder> medicationOrders = urgentCareMedicationOrderRepository
                .findByEncounterIdAndStatusOrderByCreatedDateAsc(encounter.getId(), MedicationOrderStatus.ADMINISTERED);

        // batch-resolve every LOV code used across medications (dose units + frequencies)
        // in a single lookup instead of one query per medication per field
        List<String> medicationLovKeys = medicationOrders.stream()
                .flatMap(m -> java.util.stream.Stream.of(m.getDoseUnit(), m.getFrequency()))
                .filter(key -> key != null && !key.isBlank())
                .distinct()
                .toList();
        Map<String, String> medicationLovDisplay = lovLookupService.findDisplayValues(medicationLovKeys);

        List<Map<String, String>> medicationsOnAdmission = medicationOrders.stream()
                .<Map<String, String>>map(m -> Map.of(
                        "name", m.getActiveIngredient() != null ? m.getActiveIngredient().getName() : "Unknown",
                        "dose", buildDoseText(m.getDose(), resolveLov(m.getDoseUnit(), medicationLovDisplay)),
                        "frequency", resolveLov(m.getFrequency(), medicationLovDisplay)
                ))
                .collect(Collectors.toList());

        String admissionDate = encounter.getEncounterDate() != null ? encounter.getEncounterDate().toString() : "";
        String dischargeDate = encounter.getDischargeAt() != null ? encounter.getDischargeAt().toLocalDate().toString() : null;

        return new PatientRecordDTO(
                String.valueOf(patient.getId()),
                buildAge(patient.getDateOfBirth()),
                patient.getSexAtBirth() != null ? patient.getSexAtBirth().name() : "Unknown",
                admissionDate,
                dischargeDate,
                primaryDiagnosis,
                secondaryDiagnoses,
                allergyNames,
                medicationsOnAdmission
        );
    }

    private ClinicalDocumentationDTO buildClinicalDocumentation(PatientEncounter encounter) {
        List<String> progressNotes = progressNoteRepository
                .findByEncounterIdAndCancelledByIsNullOrderByCreatedDateAsc(encounter.getId())
                .stream()
                .map(ProgressNote::getNoteText)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.toList());

        List<Map<String, String>> proceduresPerformed = patientProcedureRepository
                .findByEncounterIdAndStatusOrderByScheduledDateTimeAsc(encounter.getId(), ProcStatus.REQUESTED.name())
                .stream()
                .<Map<String, String>>map(p -> Map.of(
                        "name", p.getProcedure() != null ? p.getProcedure().getName() : "Unknown",
                        "date", p.getScheduledDateTime() != null
                                ? p.getScheduledDateTime().atZone(ZoneId.systemDefault()).toLocalDate().toString()
                                : "",
                        "result", p.getResult() != null ? p.getResult() : ""
                ))
                .collect(Collectors.toList());

        // fetched once and shared by both lab results and imaging results, instead of each
        // building its own copy - same for the DiagnosticTest lookups both of them need
        List<DiagnosticOrderTest> orderTests = fetchEncounterOrderTests(encounter.getId());
        Map<Long, DiagnosticOrderTest> orderTestById = indexById(orderTests, DiagnosticOrderTest::getId);
        Map<Long, DiagnosticTest> testById = fetchTestsByIds(orderTests.stream()
                .map(DiagnosticOrderTest::getTestId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        Map<String, Object> labResults = buildLabResults(orderTestById, testById);
        List<Map<String, String>> imagingResults = buildImagingResults(orderTests, orderTestById, testById);
        List<String> consultationNotes = buildConsultationNotes(encounter.getId());

        return new ClinicalDocumentationDTO(
                progressNotes,
                encounter.getHistoryOfPresentIllness(),
                List.of(),
                proceduresPerformed,
                labResults,
                imagingResults,
                consultationNotes
        );
    }

    private List<String> buildConsultationNotes(Long encounterId) {
        return consultationRepository
                .findByEncounterIdAndStatusNotOrderByCreatedDateAsc(encounterId, ConsultationStatus.CANCELLED)
                .stream()
                .map(this::formatConsultationNote)
                .filter(note -> note != null && !note.isBlank())
                .collect(Collectors.toList());
    }

    private String formatConsultationNote(Consultation consultation) {
        String speciality = consultation.getConsultantSpeciality();
        String content = consultation.getConsultationContent();
        String response = consultation.getResponseText();

        if (content == null || content.isBlank()) {
            return null;
        }

        String prefix = speciality != null && !speciality.isBlank() ? speciality + ": " : "";
        String note = prefix + content;

        return response != null && !response.isBlank() ? note + " - Response: " + response : note;
    }

    private List<DiagnosticOrderTest> fetchEncounterOrderTests(Long encounterId) {
        List<DiagnosticOrder> orders = diagnosticOrderRepository.findByEncounterIdOrderByCreatedDateAsc(encounterId);

        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(DiagnosticOrder::getId).toList();

        return diagnosticOrderTestRepository.findByOrderIdIn(orderIds);
    }

    private Map<Long, DiagnosticTest> fetchTestsByIds(List<Long> testIds) {
        if (testIds.isEmpty()) {
            return Map.of();
        }
        return indexById(diagnosticTestRepository.findAllById(testIds), DiagnosticTest::getId);
    }

    private <T, K> Map<K, T> indexById(List<T> items, Function<T, K> idExtractor) {
        return items.stream().collect(Collectors.toMap(idExtractor, item -> item, (a, b) -> a));
    }

    // AI service expects lab_results as a flat "{test_name: value}" dict (see
    // AI-Services/DischargeReportGenerator/models/schemas.py -> lab_results: Dict[str, Any]),
    // not a list of result objects - so this builds one entry per distinct test/profile,
    // keeping only the most recent result when the same test was run more than once.
    // Every lookup (profiles, LOV codes) is batched up front to avoid one query per result.
    private Map<String, Object> buildLabResults(Map<Long, DiagnosticOrderTest> orderTestById, Map<Long, DiagnosticTest> testById) {
        if (orderTestById.isEmpty()) {
            return Map.of();
        }

        List<DiagnosticOrderTestResult> results = diagnosticOrderTestResultRepository
                .findByOrderTestIdInOrderByCreatedDateDesc(new ArrayList<>(orderTestById.keySet()));

        if (results.isEmpty()) {
            return Map.of();
        }

        List<Long> profileIds = results.stream()
                .map(DiagnosticOrderTestResult::getProfileTestId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, DiagnosticTestProfile> profileById = profileIds.isEmpty()
                ? Map.of()
                : indexById(diagnosticTestProfileRepository.findAllById(profileIds), DiagnosticTestProfile::getId);

        Map<String, String> lovDisplayByKey = buildLabResultLovLookup(results, profileById);

        Map<String, Object> labResults = new LinkedHashMap<>();

        // ordered DESC by createdDate, so the first result seen per key is the most recent one
        for (DiagnosticOrderTestResult result : results) {
            DiagnosticTestProfile profile = profileById.get(result.getProfileTestId());

            String key = resolveLabResultKey(profile, result, orderTestById, testById);
            if (key == null || labResults.containsKey(key)) {
                continue;
            }

            String value = resolveLabResultValue(profile, result, lovDisplayByKey);
            if (value != null && !value.isBlank()) {
                labResults.put(key, value);
            }
        }

        return labResults;
    }

    // Collects every LOV code these results could need (result units + LOV-typed result values)
    // and resolves them all in one batch call instead of per-result lookups.
    private Map<String, String> buildLabResultLovLookup(
            List<DiagnosticOrderTestResult> results,
            Map<Long, DiagnosticTestProfile> profileById
    ) {
        java.util.stream.Stream<String> unitKeys = profileById.values().stream()
                .map(DiagnosticTestProfile::getResultUnit)
                .filter(key -> key != null && !key.isBlank());

        java.util.stream.Stream<String> lovValueKeys = results.stream()
                .filter(result -> {
                    DiagnosticTestProfile profile = profileById.get(result.getProfileTestId());
                    return profile != null && profile.getResultType() == TestResultType.LOV;
                })
                .map(DiagnosticOrderTestResult::getResultValueText)
                .filter(key -> key != null && !key.isBlank());

        List<String> allKeys = java.util.stream.Stream.concat(unitKeys, lovValueKeys).distinct().toList();

        return lovLookupService.findDisplayValues(allKeys);
    }

    // Radiology reports (imaging) live in DiagnosticOrderTestReport, keyed by the same
    // order_test_id as lab results - scoped here to order tests whose test type is RADIOLOGY.
    private List<Map<String, String>> buildImagingResults(
            List<DiagnosticOrderTest> orderTests,
            Map<Long, DiagnosticOrderTest> orderTestById,
            Map<Long, DiagnosticTest> testById
    ) {
        if (orderTests.isEmpty()) {
            return List.of();
        }

        List<Long> radiologyOrderTestIds = orderTests.stream()
                .filter(t -> isRadiologyTest(t.getTestId(), testById))
                .map(DiagnosticOrderTest::getId)
                .toList();

        if (radiologyOrderTestIds.isEmpty()) {
            return List.of();
        }

        return diagnosticOrderTestReportRepository
                .findByOrderTestIdInOrderByCreatedDateDesc(radiologyOrderTestIds)
                .stream()
                .map(report -> Map.entry(report, stripHtml(report.getReport())))
                .filter(entry -> !entry.getValue().isBlank())
                .map(entry -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("study", resolveImagingStudyName(entry.getKey(), orderTestById, testById));
                    item.put("findings", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private boolean isRadiologyTest(Long testId, Map<Long, DiagnosticTest> testById) {
        if (testId == null) {
            return false;
        }
        DiagnosticTest test = testById.get(testId);
        return test != null && TestType.RADIOLOGY.name().equalsIgnoreCase(test.getType());
    }

    private String resolveImagingStudyName(
            DiagnosticOrderTestReport report,
            Map<Long, DiagnosticOrderTest> orderTestById,
            Map<Long, DiagnosticTest> testById
    ) {
        DiagnosticOrderTest orderTest = orderTestById.get(report.getOrderTestId());
        DiagnosticTest test = orderTest != null ? testById.get(orderTest.getTestId()) : null;
        String testName = test != null ? test.getName() : "Unknown";

        String date = report.getCreatedDate() != null
                ? report.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate().toString()
                : null;

        return date != null ? testName + " (" + date + ")" : testName;
    }

    private String resolveLabResultKey(
            DiagnosticTestProfile profile,
            DiagnosticOrderTestResult result,
            Map<Long, DiagnosticOrderTest> orderTestById,
            Map<Long, DiagnosticTest> testById
    ) {
        if (profile != null && profile.getName() != null && !profile.getName().isBlank()) {
            return profile.getName();
        }

        DiagnosticOrderTest orderTest = orderTestById.get(result.getOrderTestId());
        DiagnosticTest test = orderTest != null ? testById.get(orderTest.getTestId()) : null;

        return test != null ? test.getName() : null;
    }

    private String resolveLabResultValue(
            DiagnosticTestProfile profile,
            DiagnosticOrderTestResult result,
            Map<String, String> lovDisplayByKey
    ) {
        String value = resolveResultValue(profile, result, lovDisplayByKey);
        if (value == null || value.isBlank()) {
            return null;
        }

        String unit = profile != null ? resolveLov(profile.getResultUnit(), lovDisplayByKey) : "";
        return unit != null && !unit.isBlank() ? value + " " + unit : value;
    }

    private String resolveResultValue(
            DiagnosticTestProfile profile,
            DiagnosticOrderTestResult result,
            Map<String, String> lovDisplayByKey
    ) {
        if (profile != null && profile.getResultType() == TestResultType.LOV) {
            return resolveLov(result.getResultValueText(), lovDisplayByKey);
        }

        if (result.getResultValueNumber() != null) {
            return result.getResultValueNumber().toString();
        }

        return result.getResultValueText();
    }

    private String buildDoseText(Long dose, String doseUnit) {
        if (dose == null) {
            return "";
        }
        return doseUnit != null && !doseUnit.isBlank() ? dose + " " + doseUnit : String.valueOf(dose);
    }

    private String resolveLov(String key, Map<String, String> lovDisplayByKey) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String displayValue = lovDisplayByKey.get(key);
        return displayValue != null ? displayValue : key;
    }

    private String resolveAllergyName(PatientAllergies allergy) {
        if (allergy.getAllergen() != null) {
            return allergy.getAllergen().getName();
        }
        if (allergy.getAllergenName() != null && !allergy.getAllergenName().isBlank()) {
            return allergy.getAllergenName();
        }
        if (allergy.getMedicationClass() != null) {
            return allergy.getMedicationClass().getName();
        }
        return null;
    }

    private Integer buildAge(java.util.Date dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }
        LocalDate birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
