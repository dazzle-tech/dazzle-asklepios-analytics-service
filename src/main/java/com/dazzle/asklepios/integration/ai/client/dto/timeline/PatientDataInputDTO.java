package com.dazzle.asklepios.integration.ai.client.dto.timeline;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record PatientDataInputDTO(
        @JsonProperty("patient_id")
        String patientId,

        DemographicsDTO demographics,
        List<DiagnosisEntryDTO> diagnoses,
        List<MedicationEntryDTO> medications,

        @JsonProperty("lab_results")
        List<LabResultEntryDTO> labResults,

        List<VitalEntryDTO> vitals,
        List<ProcedureEntryDTO> procedures,
        List<EncounterEntryDTO> encounters,
        List<AllergyEntryDTO> allergies
) {}
