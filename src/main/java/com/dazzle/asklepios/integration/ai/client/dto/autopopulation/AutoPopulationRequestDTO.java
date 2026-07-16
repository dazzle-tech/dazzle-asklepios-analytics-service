package com.dazzle.asklepios.integration.ai.client.dto.autopopulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record AutoPopulationRequestDTO(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("user_context") UserContextDTO userContext,
        @JsonProperty("input_language") String inputLanguage,
        @JsonProperty("output_language") String outputLanguage,
        @JsonProperty("user_text") String userText,
        @JsonProperty("patient_data") Map<String, Object> patientData,
        @JsonProperty("expected_output") List<String> expectedOutput
) {}
