package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SepsisProbability24h(
        Integer probability,
        @JsonProperty("risk_level") String riskLevel,
        String confidence,
        @JsonProperty("primary_drivers") List<String> primaryDrivers,
        @JsonProperty("mitigating_factors") List<String> mitigatingFactors
) {

}
