package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.integration.ai.client.ClinicalSummaryClient;
import com.dazzle.asklepios.integration.ai.client.dto.summary.SummaryPatientDataDTO;
import com.dazzle.asklepios.integration.ai.client.dto.summary.SummaryRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.summary.SummaryResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.summary.SummarySurgeryDTO;
import com.dazzle.asklepios.integration.ai.controller.vm.PatientClinicalSummaryRequestVM;
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
public class ClinicalSummaryAiService {

    private static final Logger LOG = LoggerFactory.getLogger(ClinicalSummaryAiService.class);

    private final ClinicalSummaryClient clinicalSummaryClient;
    private final PatientRepository patientRepository;
    private final PatientEncounterRepository patientEncounterRepository;
    private final PatientAiContextBuilderService patientAiContextBuilderService;

    public SummaryResponseDTO getClinicalSummary(PatientClinicalSummaryRequestVM request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found",
                        "patient",
                        "notfound"
                ));

        PatientEncounter encounter = patientEncounterRepository.findById(request.encounterId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Encounter not found",
                        "encounter",
                        "notfound"
                ));

        SummaryRequestDTO aiRequest = new SummaryRequestDTO(
                UUID.randomUUID().toString(),
                buildSummaryContext(patient, encounter)
        );

        LOG.info(
                "Clinical summary request for patientId={} encounterId={}",
                patient.getId(),
                encounter.getId()
        );

        return clinicalSummaryClient.summarize(aiRequest);
    }
    public SummaryPatientDataDTO buildSummaryContext(Patient patient, PatientEncounter encounter) {
        return new SummaryPatientDataDTO(
                patientAiContextBuilderService.calculateAge(patient.getDateOfBirth()),
                patient.getSexAtBirth() != null ? patient.getSexAtBirth().toString() : "Unknown",
                patientAiContextBuilderService.buildDiagnosis(encounter.getId()),
                patientAiContextBuilderService.buildSymptoms(encounter),
                patientAiContextBuilderService.buildCurrentMedications(patient.getId()),
               buildSummarySurgeries(patient.getId()),
                patientAiContextBuilderService.buildAllergies(patient.getId()),
                List.of(), // Medical_Warnings
                List.of(), // Problems
                patientAiContextBuilderService.buildVitals(encounter.getId()),
                patientAiContextBuilderService.buildLabResults(patient.getId())
        );
    }

    private List<SummarySurgeryDTO> buildSummarySurgeries(Long patientId) {
        return patientAiContextBuilderService.buildSurgeries(patientId)
                .stream()
                .map(surgery -> new SummarySurgeryDTO(
                        surgery.name(),
                        surgery.status()
                ))
                .toList();
    }
}