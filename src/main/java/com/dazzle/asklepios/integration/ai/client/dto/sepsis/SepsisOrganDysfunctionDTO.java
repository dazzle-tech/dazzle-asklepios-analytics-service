package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public record SepsisOrganDysfunctionDTO(
        SepsisAnalysisObjectDTO cardiovascular,
        SepsisAnalysisObjectDTO respiratory,
        SepsisAnalysisObjectDTO renal,
        SepsisAnalysisObjectDTO hepatic,
        SepsisAnalysisObjectDTO hematologic,
        SepsisAnalysisObjectDTO metabolic
) {
}
