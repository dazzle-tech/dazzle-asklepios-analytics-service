
package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientDiagnosis;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientPrescriptionMedication;
import com.dazzle.asklepios.domain.PrescriptionInstruction;
import com.dazzle.asklepios.domain.enumeration.DiagnosisType;
import com.dazzle.asklepios.domain.enumeration.PrescriptionInstructionsType;
import com.dazzle.asklepios.domain.enumeration.PrescriptionStatus;
import com.dazzle.asklepios.integration.ai.client.MedicationTestOrdersValidationClient;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.AllergyDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.DiagnosisDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.EncounterDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.MedicationLookupRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.MedicationValidationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.PatientDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.TestOrdersLookupRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.TestValidationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.ValidationResponseDTO;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.PatientAllergyRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientDiagnosisRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientObservationsComplaintsRepository;
import com.dazzle.asklepios.repository.PatientPrescriptionRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.PrescriptionMedicationRepository;
import com.dazzle.asklepios.repository.PrescriptionInstructionRepository;
import com.dazzle.asklepios.repository.EncounterPlanRepository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicationTestOrdersService {

    private static final Logger LOG = LoggerFactory.getLogger(MedicationTestOrdersService.class);

    private final MedicationTestOrdersValidationClient medicationTestOrdersValidationClient;
    private final PatientRepository patientRepository;
    private final PatientEncounterRepository patientEncounterRepository;
    private final PatientDiagnosisRepository patientDiagnosisRepository;
    private final PrescriptionMedicationRepository prescriptionMedicationRepository;
    private final PrescriptionInstructionRepository prescriptionInstructionRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final ActiveIngredientsRepository activeIngredientsRepository;
    private final PatientAllergyRepository patientAllergyRepository;
    private final ApLovValueRepository apLovValueRepository;

    private static final String UNKNOWN = "Unknown";

    public ValidationResponseDTO validateMedication(MedicationLookupRequestDTO request) {
        LOG.debug("Medication validation request={} ", request);
        MedicationValidationRequestDTO payload = buildMedicationPayload(request);
        return medicationTestOrdersValidationClient.validateMedication(payload);
    }

    public ValidationResponseDTO validateTests(TestOrdersLookupRequestDTO request) {
        LOG.error("Tests Order validation request={} ", request);
        TestValidationRequestDTO payload = buildTestPayload(request);
        return medicationTestOrdersValidationClient.validateTests(payload);
    }

    private MedicationValidationRequestDTO buildMedicationPayload(MedicationLookupRequestDTO request) {
        Patient patient = getPatient(request.patientId());
        PatientEncounter encounter = getEncounter(request.encounterId());
        String primaryDiagnosis = getPrimaryDiagnosis(encounter.getId());
        List<DiagnosisDTO> listOfDiagnosis = buildPatientDiagnosis(patient.getId());

        PatientDTO patientDTO = new PatientDTO(
                safe(patient.getMedicalRecordNumber()),
                buildFullName(patient),
                patient.getSexAtBirth() != null ? patient.getSexAtBirth() : "Unknown",
                patient.getDateOfBirth() != null
                        ? patient.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        : ""
        );

        EncounterDTO encounterDTO = new EncounterDTO(
                safe(encounter.getId()),
                encounter.getEncounterType() != null ? encounter.getEncounterType() : "",
                encounter.getEncounterDate() != null ? encounter.getEncounterDate().toString() : "",
                encounter.getChiefComplaint(),
                buildAge(patient.getDateOfBirth()),
                primaryDiagnosis
        );

        List<String> medications = prescriptionMedicationRepository
                .findAllByPrescriptionHeaderIdAndStatusNotOrderByIdAsc(request.prescriptionId(), PrescriptionStatus.CANCELLED.name())
                .stream()
                .map(this::formatMedication)
                .toList();

        return new MedicationValidationRequestDTO(
                patientDTO,
                encounterDTO,
                listOfDiagnosis,
                medications,
                buildActiveAllergies(patient.getId())
        );
    }

    private TestValidationRequestDTO buildTestPayload(TestOrdersLookupRequestDTO request) {
        Patient patient = getPatient(request.patientId());
        PatientEncounter encounter = getEncounter(request.encounterId());
        String primaryDiagnosis = getPrimaryDiagnosis(encounter.getId());
        List<DiagnosisDTO> listOfDiagnosis = buildPatientDiagnosis(patient.getId());

        PatientDTO patientDTO = new PatientDTO(
                safe(patient.getMedicalRecordNumber()),
                buildFullName(patient),
                patient.getSexAtBirth() != null ? patient.getSexAtBirth() : "Unknown",
                patient.getDateOfBirth() != null
                        ? patient.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        : ""
        );

        EncounterDTO encounterDTO = new EncounterDTO(
                safe(encounter.getId()),
                encounter.getEncounterType() != null ? encounter.getEncounterType() : "",
                encounter.getEncounterDate() != null ? encounter.getEncounterDate().toString() : "",
                encounter.getChiefComplaint(),
                buildAge(patient.getDateOfBirth()),
                primaryDiagnosis
        );

        List<String> tests = diagnosticOrderRepository.findByOrderNumber(request.orderNumber())
                .stream()
                .flatMap(order -> buildOrderTests(order).stream())
                .toList();

        return new TestValidationRequestDTO(
                patientDTO,
                encounterDTO,
                listOfDiagnosis,
                tests
        );
    }

    private Patient getPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundAlertException("Patient not found with id " + patientId, "patient", "notfound"));
    }

    private PatientEncounter getEncounter(Long encounterId) {
        return patientEncounterRepository.findById(encounterId)
                .orElseThrow(() -> new NotFoundAlertException("Encounter not found with id " + encounterId, "encounter", "notfound"));
    }

    private String buildFullName(Patient patient) {
        return java.util.stream.Stream.of(
                        patient.getFirstName(),
                        patient.getSecondName(),
                        patient.getThirdName(),
                        patient.getLastName()
                )
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(" "));
    }

    private String buildAge(java.util.Date dateOfBirth) {
        if (dateOfBirth == null) {
            return "";
        }
        LocalDate birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Period period = Period.between(birthDate, LocalDate.now());
        return period.getYears() + "y " + period.getMonths() + "m " + period.getDays() + "d";
    }

    private String getPrimaryDiagnosis(Long encounterId) {
        Optional<PatientDiagnosis> diagnosis = patientDiagnosisRepository.findByEncounterIdAndType(encounterId, DiagnosisType.PRIMARY.name());
        if (diagnosis.isPresent()) {
            PatientDiagnosis d = diagnosis.get();
            String code = d.getDiagnosis() != null ? d.getDiagnosis().getIcdCode() : "";
            String desc = d.getDiagnosis() != null ? d.getDiagnosis().getIcdShortDescription() : "";
            return (code + "," + desc).trim();
        }
        return "";
    }

    private List<DiagnosisDTO> buildPatientDiagnosis(Long patientId) {
        return patientDiagnosisRepository.findByPatientId(patientId)
                .stream()
                .map(this::formatDiagnosis)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    // Criticality and reactions are stored as LOV keys; the AI expects their display names
    private List<AllergyDTO> buildActiveAllergies(Long patientId) {
        List<PatientAllergies> allergies =
                patientAllergyRepository.findAllByPatientIdAndStatusOrderByIdAsc(patientId, "ACTIVE");
        if (allergies.isEmpty()) {
            return List.of();
        }

        List<String> lovKeys = allergies.stream()
                .flatMap(a -> Stream.concat(Stream.of(a.getCriticality()), splitKeys(a.getAllergicReactions()).stream()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, String> lovNames = lovKeys.isEmpty() ? Map.of() : apLovValueRepository.findByKeyIn(lovKeys).stream()
                .filter(lov -> lov.getLovDisplayVale() != null)
                .collect(Collectors.toMap(ApLovValue::getKey, ApLovValue::getLovDisplayVale, (a, b) -> a));

        return allergies.stream()
                .map(allergy -> toAllergyDTO(allergy, lovNames))
                .filter(Objects::nonNull)
                .toList();
    }

    // The AI accepts any value but rejects blank required fields; criticality is optional in our form
    private AllergyDTO toAllergyDTO(PatientAllergies allergy, Map<String, String> lovNames) {
        String name = allergy.getAllergenName();
        if ((name == null || name.isBlank()) && allergy.getAllergen() != null) {
            name = allergy.getAllergen().getName();
        }
        // Map.of() throws on a null key, so a missing criticality skips the lookup
        String criticality = allergy.getCriticality() == null
                ? UNKNOWN
                : Optional.ofNullable(lovNames.get(allergy.getCriticality())).orElse(UNKNOWN);

        if (name == null || name.isBlank()) {
            LOG.warn("[MED_VALIDATION] Skipping allergy id={} with no allergen name", allergy.getId());
            return null;
        }

        List<String> reactions = splitKeys(allergy.getAllergicReactions()).stream()
                .map(lovNames::get)
                .filter(Objects::nonNull)
                .toList();

        return new AllergyDTO(name.trim(), orUnknown(allergy.getAllergenType()), orUnknown(allergy.getSeverity()),
                criticality, reactions, orUnknown(allergy.getStatus()));
    }

    private String orUnknown(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
    }

    private List<String> splitKeys(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(key -> !key.isEmpty())
                .toList();
    }

    private DiagnosisDTO formatDiagnosis(PatientDiagnosis diagnosis) {
        if (diagnosis == null || diagnosis.getDiagnosis() == null) {
            return null;
        }
        String code = safe(diagnosis.getDiagnosis().getIcdCode());
        String desc = safe(diagnosis.getDiagnosis().getIcdShortDescription());
        String type = diagnosis.getType() != null ? diagnosis.getType() : "";
        String value = (code + "," + desc).trim();
        if (value.isBlank()) {
            return null;
        }
        return new DiagnosisDTO(type, value);
    }

    private String formatMedication(PatientPrescriptionMedication medication) {
        String medicationName = medication.getMedicationDisplayName() != null ? medication.getMedicationDisplayName() : "";
        String instruction = resolveInstruction(medication);
        ActiveIngredients activeIngredient =
                activeIngredientsRepository.findById(medication.getActiveIngredient().getId())
                        .orElse(null);
        String activeIngredientName = activeIngredient != null ? activeIngredient.getName() : null;
        return "Medication Name: " + medicationName + " | Active Ingredients: " + activeIngredientName + " - " + safe(instruction);
    }

    // Tests of one order; reason keys are resolved to their LOV names in one query
    private List<String> buildOrderTests(DiagnosticOrder order) {
        List<DiagnosticOrderTest> orderTests = diagnosticOrderTestRepository.findByOrderIdOrderByIdAsc(order.getId());
        List<String> reasonKeys = orderTests.stream()
                .map(DiagnosticOrderTest::getReason)
                .filter(key -> key != null && !key.isBlank())
                .distinct()
                .toList();
        Map<String, String> reasonNames = reasonKeys.isEmpty() ? Map.of() : apLovValueRepository.findByKeyIn(reasonKeys).stream()
                .filter(lov -> lov.getLovDisplayVale() != null)
                .collect(Collectors.toMap(ApLovValue::getKey, ApLovValue::getLovDisplayVale, (a, b) -> a));
        String priority = Boolean.TRUE.equals(order.getIsUrgent()) ? "Urgent" : "Regular";
        return orderTests.stream()
                // Map.of() throws on a null key, so tests without a reason skip the lookup
                .map(test -> formatTest(test, test.getReason() == null ? null : reasonNames.get(test.getReason()), priority))
                .toList();
    }

    private String formatTest(DiagnosticOrderTest test, String reason, String priority) {
        DiagnosticTest diagnosticTest = diagnosticTestRepository.findById(test.getTestId()).orElse(null);
        String testName = diagnosticTest != null ? diagnosticTest.getName() : "";
        String internalCode = diagnosticTest != null ? diagnosticTest.getInternalCode() : "";
        String orderType = diagnosticTest != null ? formatTestType(diagnosticTest.getType()) : UNKNOWN;
        String line = "Order Type: " + orderType + " | Test Name: " + testName + " | Internal Code: " + internalCode + " | Status: " + safe(String.valueOf(test.getStatus()));
        // Reason is optional, like in the AI service sample
        if (reason != null && !reason.isBlank()) {
            line += " | Reason: " + reason;
        }
        return line + " | Priority: " + priority;
    }

    // LABORATORY -> Laboratory, RADIOLOGY -> Radiology
    private String formatTestType(String type) {
        if (type == null || type.isBlank()) {
            return UNKNOWN;
        }
        return Arrays.stream(type.trim().toLowerCase().split("_"))
                .filter(word -> !word.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private String resolveInstruction(PatientPrescriptionMedication medication) {
        if (medication == null || medication.getInstructionsType() == null) {
            return medication != null ? medication.getInstructions() : "";
        }
        if (PrescriptionInstructionsType.MANUAL_INSTRUCTIONS.name().equals(medication.getInstructionsType())) {
            return safe(medication.getInstructions());
        }
        if (PrescriptionInstructionsType.CUSTOM_INSTRUCTIONS.name().equals(medication.getInstructionsType())) {
            return buildCustomInstruction(medication);
        }
        if (PrescriptionInstructionsType.PRE_DEFINED_INSTRUCTIONS.name().equals(medication.getInstructionsType())) {
            Long id = safeParseLong(medication.getInstructions());
            if (id == null) {
                return "";
            }
            return prescriptionInstructionRepository.findById(id)
                    .map(this::buildPredefinedInstruction)
                    .orElse("");
        }
        return safe(medication.getInstructions());
    }

    private String buildCustomInstruction(PatientPrescriptionMedication medication) {
        StringBuilder sb = new StringBuilder();
        if (medication.getDose() != null) {
            sb.append(medication.getDose().stripTrailingZeros().toPlainString());
        }
        if (medication.getDoesUnit() != null && !medication.getDoesUnit().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(medication.getDoesUnit());
        }
        return sb.toString();
    }

    private String buildPredefinedInstruction(PrescriptionInstruction instruction) {
        if (instruction == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (instruction.getDose() != null) {
            sb.append(instruction.getDose());
        }
        if (instruction.getUnit() != null && !instruction.getUnit().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(instruction.getUnit());
        }
        return sb.toString();
    }

    private Long safeParseLong(String value) {
        try {
            return value == null ? null : Long.parseLong(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safe(Long value) {
        return value == null ? "" : String.valueOf(value);
    }
}