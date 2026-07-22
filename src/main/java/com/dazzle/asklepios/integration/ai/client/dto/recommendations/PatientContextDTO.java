package com.dazzle.asklepios.integration.ai.client.dto.recommendations;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record PatientContextDTO(
        String age,
        String gender,
        String diagnosis,
        List<String> symptoms,
        List<String> medications,
        List<String> allergies,
        List<String> comorbidities,
        Map<String, String> vitals,

        @JsonProperty("lab_results")
        Map<String, Object> labResults,

        List<SurgeryDTO> surgeries,

        @JsonProperty("clinical_notes")
        String clinicalNotes
) {}