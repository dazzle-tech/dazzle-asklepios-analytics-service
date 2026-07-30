package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public record SepsisAnalysisObjectDTO(
        String value,
        String unit,
        String status,
        String trend
){
}
