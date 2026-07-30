package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SepsisKeyDecisionPoints(
        String hour,
        String trigger,
        @JsonProperty("recommended_response") String recommendedResponse
) {
}
