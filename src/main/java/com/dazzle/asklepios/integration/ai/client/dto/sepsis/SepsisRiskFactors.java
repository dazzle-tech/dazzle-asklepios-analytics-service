package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SepsisRiskFactors(
        @JsonProperty("infection_source_suspected") String infectionSourceSuspected,
        Boolean immunocompromised,
        @JsonProperty("age_risk") Boolean ageRisk,
        @JsonProperty("comorbidity_burden") String comorbidityBurden,
        @JsonProperty("contributing_factors") List<String> contributingFactors
) {
}
