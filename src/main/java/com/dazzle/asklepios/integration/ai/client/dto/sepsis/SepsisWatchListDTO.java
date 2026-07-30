package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SepsisWatchListDTO(
        String parameter,
        @JsonProperty("current_value") String currentValue,
        String unit,
        @JsonProperty("normal_range") String normalRange,
        String status,
        String trend,
        @JsonProperty("rate_of_change") String rateOfChange,
        @JsonProperty("clinical_concern") String clinicalConcern,
        @JsonProperty("recommended_action") String recommendedAction
) {
}
