package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.integration.ai.client.RecommendationsClient;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.PatientContextDTO;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.RecommendationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.RecommendationsResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.SpecialtyConsultationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.SpecialtyConsultationResponseDTO;
import com.dazzle.asklepios.integration.ai.controller.vm.PatientRecommendationRequestVM;
import com.dazzle.asklepios.integration.ai.controller.vm.PatientSpecialtyConsultationRequestVM;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationsAiService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationsAiService.class);

    private final RecommendationsClient recommendationsClient;
    private final PatientRepository patientRepository;
    private final PatientEncounterRepository patientEncounterRepository;

    private final PatientAiContextBuilderService patientAiContextBuilderService;

    public RecommendationsResponseDTO getRecommendations(PatientRecommendationRequestVM request) {
        Patient patient = getPatient(request.patientId());
        PatientEncounter encounter = getEncounter(request.encounterId());

        RecommendationRequestDTO aiRequest =
                buildAiRecommendationRequest(patient, encounter, request.focusAreas());

        LOG.info(
                "Recommendation request for patientId={} encounterId={}",
                patient.getId(),
                encounter.getId()
        );

        return recommendationsClient.getRecommendations(aiRequest);
    }

    public SpecialtyConsultationResponseDTO getSpecialtyConsultation(
            PatientSpecialtyConsultationRequestVM request
    ) {
        Patient patient = getPatient(request.patientId());
        PatientEncounter encounter = getEncounter(request.encounterId());

        SpecialtyConsultationRequestDTO aiRequest =
                new SpecialtyConsultationRequestDTO(
                        UUID.randomUUID().toString(),
                        request.specialty(),
                       buildPatientContext(patient, encounter),
                        encounter.getChiefComplaint()
                );

        LOG.info(
                "Specialty consultation request for patientId={} encounterId={} specialty={}",
                patient.getId(),
                encounter.getId(),
                request.specialty()
        );

        return recommendationsClient.getSpecialtyConsultation(aiRequest);
    }

    private Patient getPatient(Long patientId) {
        return patientRepository
                .findById(patientId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found",
                        "patient",
                        "notfound"
                ));
    }

    private PatientEncounter getEncounter(Long encounterId) {
        return patientEncounterRepository
                .findById(encounterId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Encounter not found",
                        "encounter",
                        "notfound"
                ));
    }

    private RecommendationRequestDTO buildAiRecommendationRequest(
            Patient patient,
            PatientEncounter encounter,
            List<String> focusAreas
    ) {
        return new RecommendationRequestDTO(
                UUID.randomUUID().toString(),
                buildPatientContext(patient, encounter),
                List.of("general"),
                focusAreas != null ? focusAreas : List.of()
        );
    }

    PatientContextDTO buildPatientContext(Patient patient, PatientEncounter encounter) {
        return new PatientContextDTO(
                patientAiContextBuilderService.calculateAge(patient.getDateOfBirth()),
                patient.getSexAtBirth() != null ? patient.getSexAtBirth().toString() : "Unknown",
                patientAiContextBuilderService.buildDiagnosis(encounter.getId()),
                patientAiContextBuilderService.buildSymptoms(encounter),
                patientAiContextBuilderService.buildCurrentMedications(patient.getId()),
                patientAiContextBuilderService.buildAllergies(patient.getId()),
                List.of(),
                patientAiContextBuilderService.buildVitals(encounter.getId()),
                patientAiContextBuilderService.buildLabResults(patient.getId()),
                patientAiContextBuilderService.buildSurgeries(patient.getId()),
                patientAiContextBuilderService.buildClinicalNotes(encounter.getId())
        );
    }
}
