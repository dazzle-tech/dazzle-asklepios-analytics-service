package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.domain.CurrentMedication;
import com.dazzle.asklepios.domain.MedicationCategoriesClass;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientProblem;
import com.dazzle.asklepios.domain.VitalSigns;
import com.dazzle.asklepios.domain.enumeration.PatientHistoryStatus;
import com.dazzle.asklepios.integration.ai.client.AutoPopulationClient;
import com.dazzle.asklepios.integration.ai.client.dto.AutoPopulateRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.autopopulation.AutoPopulationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.autopopulation.AutoPopulationResponseDTO;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.repository.CurrentMedicationRepository;
import com.dazzle.asklepios.repository.MedicationCategoriesClassRepository;
import com.dazzle.asklepios.repository.PatientAllergyRepository;
import com.dazzle.asklepios.repository.PatientProblemRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.VitalSignsRepository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoPopulationService {

    private static final Logger LOG = LoggerFactory.getLogger(AutoPopulationService.class);

    private static final List<String> EXPECTED_OUTPUT_FIELDS = List.of(
            "chief_complaint",
            "history_of_present_illness",
            "diagnosis",
            "medications",
            "vitals",
            "procedures",
            "allergies",
            "assessment",
            "plan",
            "past_medical_history"
    );

    private final AutoPopulationClient autoPopulationClient;
    private final PatientRepository patientRepository;
    private final CurrentMedicationRepository currentMedicationRepository;
    private final ActiveIngredientsRepository activeIngredientsRepository;
    private final PatientAllergyRepository patientAllergyRepository;
    private final MedicationCategoriesClassRepository medicationCategoriesClassRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final PatientProblemRepository patientProblemRepository;

    public AutoPopulationResponseDTO autoPopulate(AutoPopulateRequestDTO request) {
        LOG.debug(
                "Request to auto populate user text={} patient id={}",
                request.userText(),
                request.patientId()
        );
        Patient patient = getPatient(request.patientId());

        AutoPopulationRequestDTO payload = new AutoPopulationRequestDTO(
                UUID.randomUUID().toString(),
                "en",
                "en",
                request.userText(),
                buildPatientData(patient),
                EXPECTED_OUTPUT_FIELDS
        );
        return autoPopulationClient.autoPopulate(payload);
    }

    private Map<String, Object> buildPatientData(Patient patient) {
        Map<String, Object> patientData = new HashMap<>();
        patientData.put("medications", buildMedications(patient.getId()));
        patientData.put("allergies", buildAllergies(patient.getId()));
        patientData.put("vitals", buildVitals(patient.getId()));
        patientData.put("past_medical_history", buildPastMedicalHistory(patient.getId()));
        return patientData;
    }

    private List<Map<String, Object>> buildMedications(Long patientId) {
        return currentMedicationRepository
                .findByPatientIdAndStatus(patientId, PatientHistoryStatus.ACTIVE)
                .stream()
                .map(this::formatMedication)
                .toList();
    }

    private Map<String, Object> formatMedication(CurrentMedication medication) {
        String name = activeIngredientsRepository.findById(medication.getActiveIngredientId())
                .map(ActiveIngredients::getName)
                .orElse(null);

        String dosage = medication.getDosage() != null
                ? medication.getDosage().toPlainString() + (medication.getUnit() != null ? medication.getUnit().name() : "")
                : null;

        Map<String, Object> medicationData = new HashMap<>();
        medicationData.put("name", name);
        medicationData.put("dosage", dosage);
        medicationData.put("frequency", medication.getFrequency() != null ? medication.getFrequency().name() : null);
        return medicationData;
    }
    private List<String> buildAllergies(Long patientId) {
        return patientAllergyRepository.findAllByPatientId(patientId)
                .stream()
                .map(this::resolveAllergenName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    private String resolveAllergenName(PatientAllergies patientAllergy) {
        if (patientAllergy.getAllergen() != null) {
            return patientAllergy.getAllergen().getName();
        }
        if (patientAllergy.getAllergenName() != null) {
            return patientAllergy.getAllergenName();
        }
        if (patientAllergy.getMedicationClassId() != null) {
            return medicationCategoriesClassRepository.findById(patientAllergy.getMedicationClassId())
                    .map(MedicationCategoriesClass::getName)
                    .orElse(null);
        }
        return null;
    }
    private Map<String, Object> buildVitals(Long patientId) {
        return vitalSignsRepository.findFirstByPatient_IdAndIsActiveTrueOrderByCreatedDateDesc(patientId)
                .map(this::formatVitals)
                .orElseGet(Map::of);
    }

    private Map<String, Object> formatVitals(VitalSigns vitals) {
        Map<String, Object> vitalsData = new HashMap<>();
        if (vitals.getBloodPressureSystolic() != null && vitals.getBloodPressureDiastolic() != null) {
            vitalsData.put("bp", vitals.getBloodPressureSystolic() + "/" + vitals.getBloodPressureDiastolic());
        }
        if (vitals.getHeartRate() != null) {
            vitalsData.put("hr", String.valueOf(vitals.getHeartRate()));
        }
        return vitalsData;
    }

    private List<String> buildPastMedicalHistory(Long patientId) {
        return patientProblemRepository.findByPatientIdAndStatus(patientId, PatientHistoryStatus.ACTIVE)
                .stream()
                .map(PatientProblem::getCondition)
                .filter(condition -> condition != null && !condition.isBlank())
                .toList();
    }

    private Patient getPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundAlertException("Patient not found with id " + patientId, "patient", "notfound"));
    }
}
