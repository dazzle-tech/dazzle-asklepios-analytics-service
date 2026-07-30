package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SepsisStatusSummaryDTO(
        String overall,
        @JsonProperty("sirs_criteria_met")
        String sirsCriteriaMet,
        @JsonProperty("qsofa_score")
        String qsofaScore,
        @JsonProperty("sofa_score")
        String sofaScore,
        @JsonProperty("sepsis_3_criteria_met")
        String sepsis3CriteriaMet
) {
}
