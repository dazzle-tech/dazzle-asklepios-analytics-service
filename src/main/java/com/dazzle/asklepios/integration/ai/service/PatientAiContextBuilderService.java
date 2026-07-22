package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestResult;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.EncounterAssessment;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.ProgressNote;
import com.dazzle.asklepios.domain.enumeration.DiagnosisType;
import com.dazzle.asklepios.domain.enumeration.PatientAllergyStatus;
import com.dazzle.asklepios.domain.enumeration.PatientHistoryStatus;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.SurgeryDTO;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.CurrentMedicationRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestResultRepository;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.EncounterAssessmentRepository;
import com.dazzle.asklepios.repository.PatientAllergiesRepository;
import com.dazzle.asklepios.repository.PatientDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientProcedureRepository;
import com.dazzle.asklepios.repository.ProgressNoteRepository;
import com.dazzle.asklepios.repository.VitalSignsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PatientAiContextBuilderService {

    private final VitalSignsRepository vitalSignsRepository;
    private final PatientAllergiesRepository patientAllergiesRepository;
    private final PatientDiagnosisRepository patientDiagnosisRepository;
    private final CurrentMedicationRepository currentMedicationRepository;
    private final PatientProcedureRepository patientProcedureRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticOrderTestResultRepository diagnosticOrderTestResultRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final DiagnosticTestProfileRepository diagnosticTestProfileRepository;
    private final EncounterAssessmentRepository encounterAssessmentRepository;
    private final ApLovValueRepository apLovValueRepository;
    private final ProgressNoteRepository progressNoteRepository;

    public PatientAiContextBuilderService(VitalSignsRepository vitalSignsRepository, PatientAllergiesRepository patientAllergiesRepository, PatientDiagnosisRepository patientDiagnosisRepository, CurrentMedicationRepository currentMedicationRepository, PatientProcedureRepository patientProcedureRepository, DiagnosticOrderRepository diagnosticOrderRepository, DiagnosticOrderTestRepository diagnosticOrderTestRepository, DiagnosticOrderTestResultRepository diagnosticOrderTestResultRepository, DiagnosticTestRepository diagnosticTestRepository, DiagnosticTestProfileRepository diagnosticTestProfileRepository, EncounterAssessmentRepository encounterAssessmentRepository, ApLovValueRepository apLovValueRepository, ProgressNoteRepository progressNoteRepository) {
        this.vitalSignsRepository = vitalSignsRepository;
        this.patientAllergiesRepository = patientAllergiesRepository;
        this.patientDiagnosisRepository = patientDiagnosisRepository;
        this.currentMedicationRepository = currentMedicationRepository;
        this.patientProcedureRepository = patientProcedureRepository;
        this.diagnosticOrderRepository = diagnosticOrderRepository;
        this.diagnosticOrderTestRepository = diagnosticOrderTestRepository;
        this.diagnosticOrderTestResultRepository = diagnosticOrderTestResultRepository;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.diagnosticTestProfileRepository = diagnosticTestProfileRepository;
        this.encounterAssessmentRepository = encounterAssessmentRepository;
        this.apLovValueRepository = apLovValueRepository;
        this.progressNoteRepository = progressNoteRepository;
    }


    Map<String, Object> buildLabResults(Long patientId) {
        List<DiagnosticOrder> orders =
                diagnosticOrderRepository.findByPatientIdOrderByCreatedDateDesc(patientId);

        if (orders.isEmpty()) {
            return Map.of();
        }

        List<Long> orderIds = orders.stream()
                .map(DiagnosticOrder::getId)
                .toList();

        List<DiagnosticOrderTest> orderTests =
                diagnosticOrderTestRepository.findByOrderIdIn(orderIds);

        if (orderTests.isEmpty()) {
            return Map.of();
        }

        Map<Long, DiagnosticOrderTest> orderTestById = orderTests.stream()
                .collect(Collectors.toMap(
                        DiagnosticOrderTest::getId,
                        item -> item,
                        (a, b) -> a
                ));

        List<Long> orderTestIds = orderTests.stream()
                .map(DiagnosticOrderTest::getId)
                .toList();

        List<Map<String, String>> labResults =
                diagnosticOrderTestResultRepository
                        .findByOrderTestIdInOrderByCreatedDateDesc(orderTestIds)
                        .stream()
                        .limit(50)
                        .map(result -> mapLabResult(result, orderTestById))
                        .toList();

        if (labResults.isEmpty()) {
            return Map.of();
        }

        return Map.of("patient_lab_results", labResults);
    }

    Map<String, String> mapLabResult(
            DiagnosticOrderTestResult result,
            Map<Long, DiagnosticOrderTest> orderTestById
    ) {
        DiagnosticOrderTest orderTest = orderTestById.get(result.getOrderTestId());

        String testName = null;
        String status = null;

        if (orderTest != null) {
            status = orderTest.getProcessingStatus() != null
                    ? orderTest.getProcessingStatus().toString()
                    : null;

            if (orderTest.getTestId() != null) {
                testName = diagnosticTestRepository
                        .findById(orderTest.getTestId())
                        .map(DiagnosticTest::getName)
                        .orElse(null);
            }
        }

        DiagnosticTestProfile profile = diagnosticTestProfileRepository
                .findById(result.getProfileTestId())
                .orElse(null);

        Map<String, String> item = new LinkedHashMap<>();

        putIfNotNull(item, "date", result.getCreatedDate());
        putIfNotNull(item, "status", status);
        putIfNotNull(item, "test", testName);
        putIfNotNull(item, "profile", profile != null ? profile.getName() : null);
        putIfNotNull(item, "result", resolveResultValue(profile, result));
        putIfNotNull(item, "normal_range", resolveNormalRange(profile, result.getNormalRangeValue()));
        putIfNotNull(item, "marker", result.getMarker());

        return item;
    }

    List<String> buildAllergies(Long patientId) {
        return patientAllergiesRepository
                .findByPatientIdAndStatusNotOrderByCreatedDateAsc(
                        patientId,
                        PatientAllergyStatus.CANCELLED
                )
                .stream()
                .map(allergy -> {
                    String type = allergy.getAllergenType() != null
                            ? allergy.getAllergenType().name()
                            : null;

                    String allergenName = resolveAllergenName(allergy);

                    String severity = allergy.getSeverity() != null
                            ? allergy.getSeverity().name()
                            : null;

                    return Stream.of(type, allergenName, severity)
                            .filter(Objects::nonNull)
                            .filter(value -> !value.isBlank())
                            .collect(Collectors.joining(" - "));
                })
                .filter(value -> !value.isBlank())
                .toList();
    }

    String resolveAllergenName(PatientAllergies allergy) {
        if (allergy.getAllergen() != null && allergy.getAllergen().getName() != null) {
            return allergy.getAllergen().getName();
        }

        if (allergy.getMedicationClass() != null && allergy.getMedicationClass().getName() != null) {
            return allergy.getMedicationClass().getName();
        }

        return "Unknown allergen";
    }

    Map<String, String> buildVitals(Long encounterId) {
        return vitalSignsRepository
                .findFirstByEncounterIdAndIsActiveTrueOrderByCreatedDateDesc(encounterId)
                .map(vital -> {
                    Map<String, String> vitals = new LinkedHashMap<>();

                    putIfNotNull(vitals, "Blood Pressure Systolic", vital.getBloodPressureSystolic());
                    putIfNotNull(vitals, "Blood Pressure Diastolic", vital.getBloodPressureDiastolic());
                    putIfNotNull(vitals, "Heart Rate", vital.getHeartRate());
                    putIfNotNull(vitals, "Temperature", vital.getTemperature());
                    putIfNotNull(vitals, "Oxygen Saturation", vital.getOxygenSaturation());
                    putIfNotNull(vitals, "Respiratory Rate", vital.getRespiratoryRate());
                    putIfNotNull(vitals, "Notes", vital.getNotes());

                    return vitals;
                })
                .orElse(Map.of());
    }

    String mapProcedureStatus(String status) {
        if (status == null || status.isBlank()) {
            return "scheduled";
        }

        return switch (status.trim().toUpperCase()) {
            case "REQUESTED", "PENDING", "BOOKED", "SCHEDULED" -> "scheduled";
            case "COMPLETED", "DONE", "FINISHED" -> "completed";
            case "CANCELLED", "CANCELED" -> "cancelled";
            default -> status.toLowerCase();
        };
    }

    List<SurgeryDTO> buildSurgeries(Long patientId) {
        return patientProcedureRepository
                .findByPatientIdOrderByScheduledDateTimeDesc(patientId)
                .stream()
                .map(procedure -> {
                    String procedureName =
                            procedure.getProcedure() != null
                                    ? procedure.getProcedure().getName()
                                    : "Unknown Procedure";

                    String status = mapProcedureStatus(procedure.getStatus());

                    return new SurgeryDTO(procedureName, status);
                })
                .toList();
    }

    String buildDiagnosis(Long encounterId) {
        return patientDiagnosisRepository
                .findByEncounterIdAndType(encounterId, DiagnosisType.PRIMARY)
                .map(patientDiagnosis -> patientDiagnosis.getDiagnosis() != null
                        ? patientDiagnosis.getDiagnosis().getIcdShortDescription()
                        : null
                )
                .filter(value -> value != null && !value.isBlank())
                .orElse("General assessment");
    }

    List<String> buildSymptoms(PatientEncounter encounter) {
        if (encounter.getChiefComplaint() == null || encounter.getChiefComplaint().isBlank()) {
            return List.of();
        }

        return List.of(encounter.getChiefComplaint());
    }

    List<String> buildCurrentMedications(Long patientId) {
        return currentMedicationRepository
                .findByPatientIdAndStatusOrderByCreatedDateAsc(
                        patientId,
                        PatientHistoryStatus.ACTIVE
                )
                .stream()
                .map(med -> {
                    String medicationName = med.getActiveIngredient() != null
                            ? med.getActiveIngredient().getName()
                            : null;


                    String startDate = med.getStartDate() != null
                            ? "Started: " + med.getStartDate()
                            : null;

                    return Stream.of(
                                    medicationName,
                                    startDate
                            )
                            .filter(Objects::nonNull)
                            .filter(value -> !value.isBlank())
                            .collect(Collectors.joining(" - "));
                })
                .filter(value -> !value.isBlank())
                .toList();
    }

    void putIfNotNull(Map<String, String> map, String key, Object value) {
        if (value != null && !value.toString().isBlank()) {
            map.put(key, value.toString());
        }
    }

    String resolveResultValue(
            DiagnosticTestProfile profile,
            DiagnosticOrderTestResult result
    ) {
        if (profile != null && profile.getResultType() == TestResultType.LOV) {
            return apLovValueRepository
                    .findById(String.valueOf(result.getResultValueText()))
                    .map(ApLovValue::getLovDisplayVale)
                    .orElse(result.getResultValueText());
        }

        if (result.getResultValueNumber() != null) {
            return result.getResultValueNumber().toString();
        }

        return result.getResultValueText();
    }

    String resolveNormalRange(
            DiagnosticTestProfile profile,
            String value
    ) {
        if (value == null) {
            return null;
        }

        if (profile != null && profile.getResultType() == TestResultType.LOV) {
            return apLovValueRepository
                    .findById(String.valueOf(value))
                    .map(ApLovValue::getLovDisplayVale)
                    .orElse(value);
        }

        return value;
    }

    public String calculateAge(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return "Unknown";
        }

        LocalDate birthDate = dateOfBirth.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        Period period = Period.between(birthDate, LocalDate.now());

        return period.getYears() + " Years " +
                period.getMonths() + " Months " +
                period.getDays() + " Days";
    }

    String buildClinicalNotes(Long encounterId) {
        String assessment = encounterAssessmentRepository
                .findFirstByEncounterIdOrderByCreatedDateDesc(encounterId)
                .map(EncounterAssessment::getAssessment)
                .orElse(null);

        String progressNote = progressNoteRepository
                .findFirstByEncounterIdAndCancelledByIsNullOrderByCreatedDateDesc(encounterId)
                .map(ProgressNote::getNoteText)
                .orElse(null);

        return Stream.of(
                        assessment != null && !assessment.isBlank()
                                ? "Assessment: " + assessment
                                : null,
                        progressNote != null && !progressNote.isBlank()
                                ? "Progress Note: " + progressNote
                                : null
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

}