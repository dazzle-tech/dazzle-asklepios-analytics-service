package com.dazzle.asklepios.integration.ai.client.dto.recommendations;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecommendationRequestDTO(
        @JsonProperty("request_id")
        String requestId,

        @JsonProperty("patient_context")
        PatientContextDTO patientContext,

        @JsonProperty("recommendation_types")
        List<String> recommendationTypes,

        @JsonProperty("focus_areas")
        List<String> focusAreas
) {}