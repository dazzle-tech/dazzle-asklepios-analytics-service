package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)

public record SepsisTimelineAnalysisDTO(
        String time,
        String event,
        @JsonProperty("affected_parameters")
        List<String> affectedParameters,
        String severity,
        @JsonProperty("clinical_significance")
        String clinicalSignificance
) {
}
