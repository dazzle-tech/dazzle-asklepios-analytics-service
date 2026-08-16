
package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.ActiveIngredients;
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
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.DiagnosisDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.EncounterDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.MedicationLookupRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.MedicationValidationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.PatientDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.TestOrdersLookupRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.TestValidationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.ValidationResponseDTO;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
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
import java.util.List;
import java.util.Optional;
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
                patient.getSexAtBirth() != null ? patient.getSexAtBirth().name() : "Unknown",
                patient.getDateOfBirth() != null
                        ? patient.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        : ""
        );

        EncounterDTO encounterDTO = new EncounterDTO(
                safe(encounter.getId()),
                encounter.getEncounterType() != null ? encounter.getEncounterType().name() : "",
                encounter.getEncounterDate() != null ? encounter.getEncounterDate().toString() : "",
                encounter.getChiefComplaint(),
                buildAge(patient.getDateOfBirth()),
                primaryDiagnosis
        );

        List<String> medications = prescriptionMedicationRepository
                .findAllByPrescriptionHeaderIdAndStatusNotOrderByIdAsc(request.prescriptionId(), PrescriptionStatus.CANCELLED)
                .stream()
                .map(this::formatMedication)
                .toList();

        return new MedicationValidationRequestDTO(
                patientDTO,
                encounterDTO,
                listOfDiagnosis,
                medications
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
                patient.getSexAtBirth() != null ? patient.getSexAtBirth().name() : "Unknown",
                patient.getDateOfBirth() != null
                        ? patient.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
                        : ""
        );

        EncounterDTO encounterDTO = new EncounterDTO(
                safe(encounter.getId()),
                encounter.getEncounterType() != null ? encounter.getEncounterType().name() : "",
                encounter.getEncounterDate() != null ? encounter.getEncounterDate().toString() : "",
                encounter.getChiefComplaint(),
                buildAge(patient.getDateOfBirth()),
                primaryDiagnosis
        );

        List<String> tests = diagnosticOrderRepository.findByOrderNumber(request.orderNumber())
                .stream()
                .map(DiagnosticOrder::getId)
                .flatMap(orderId -> diagnosticOrderTestRepository.findByOrderIdOrderByIdAsc(orderId).stream())
                .map(this::formatTest)
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
        Optional<PatientDiagnosis> diagnosis = patientDiagnosisRepository.findByEncounterIdAndType(encounterId, DiagnosisType.PRIMARY);
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

    private DiagnosisDTO formatDiagnosis(PatientDiagnosis diagnosis) {
        if (diagnosis == null || diagnosis.getDiagnosis() == null) {
            return null;
        }
        String code = safe(diagnosis.getDiagnosis().getIcdCode());
        String desc = safe(diagnosis.getDiagnosis().getIcdShortDescription());
        String type = diagnosis.getType() != null ? diagnosis.getType().name() : "";
        String value = (code + "," + desc).trim();
        if (value.isBlank()) {
            return null;
        }
        return new DiagnosisDTO(type, value);
    }

    private String formatMedication(PatientPrescriptionMedication medication) {
        String medicationName = medication.getMedications() != null ? medication.getMedications().getName() : "";
        String instruction = resolveInstruction(medication);
        ActiveIngredients activeIngredient =
                activeIngredientsRepository.findById(medication.getActiveIngredient().getId())
                        .orElse(null);
        String activeIngredientName = activeIngredient != null ? activeIngredient.getName() : null;
        return "Medication Name: " + medicationName + " | Active Ingredients: " + activeIngredientName + " - " + safe(instruction);
    }

    private String formatTest(DiagnosticOrderTest test) {
        DiagnosticTest diagnosticTest = diagnosticTestRepository.findById(test.getTestId()).orElse(null);
        String testName = diagnosticTest != null ? diagnosticTest.getName() : "";
        String internalCode = diagnosticTest != null ? diagnosticTest.getInternalCode() : "";;
        return "Order Type: Laboratory | Test Name: " + testName + " | Internal Code: " + internalCode + " | Status: " + safe(String.valueOf(test.getStatus()));
    }

    private String resolveInstruction(PatientPrescriptionMedication medication) {
        if (medication == null || medication.getInstructionsType() == null) {
            return medication != null ? medication.getInstructions() : "";
        }
        if (medication.getInstructionsType() == PrescriptionInstructionsType.MANUAL_INSTRUCTIONS) {
            return safe(medication.getInstructions());
        }
        if (medication.getInstructionsType() == PrescriptionInstructionsType.CUSTOM_INSTRUCTIONS) {
            return buildCustomInstruction(medication);
        }
        if (medication.getInstructionsType() == PrescriptionInstructionsType.PRE_DEFINED_INSTRUCTIONS) {
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
            sb.append(medication.getDose());
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