package com.dazzle.asklepios.integration.ai.client.dto.summary;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record SummaryPatientDataDTO(
        @JsonProperty("Age")
        String age,

        @JsonProperty("Gender")
        String gender,

        @JsonProperty("Diagnosis")
        String diagnosis,

        @JsonProperty("Symptoms")
        List<String> symptoms,

        @JsonProperty("Medications")
        List<String> medications,

        @JsonProperty("Surgeries")
        List<SummarySurgeryDTO> surgeries,

        @JsonProperty("Allergies")
        List<String> allergies,

        @JsonProperty("Medical_Warnings")
        List<String> medicalWarnings,

        @JsonProperty("Problems")
        List<String> problems,

        @JsonProperty("Vitals")
        Map<String, String> vitals,

        @JsonProperty("Lab_Results")
        Map<String, Object> labResults
) {}