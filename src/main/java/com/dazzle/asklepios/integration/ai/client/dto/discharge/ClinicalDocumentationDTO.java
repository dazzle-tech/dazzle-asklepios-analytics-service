package com.dazzle.asklepios.integration.ai.client.dto.discharge;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ClinicalDocumentationDTO(
        @JsonProperty("progress_notes")
        List<String> progressNotes,

        @JsonProperty("admission_notes")
        String admissionNotes,

        @JsonProperty("prior_discharge_summaries")
        List<String> priorDischargeSummaries,

        @JsonProperty("procedures_performed")
        List<Map<String, String>> proceduresPerformed,

        @JsonProperty("lab_results")
        Map<String, Object> labResults,

        @JsonProperty("imaging_results")
        List<Map<String, String>> imagingResults,

        @JsonProperty("consultation_notes")
        List<String> consultationNotes
) {}
