package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

public record SepsisOrganDysfunctionDTO(
        SepsisAnalysisObjectDTO cardiovascular,
        SepsisAnalysisObjectDTO respiratory,
        SepsisAnalysisObjectDTO renal,
        SepsisAnalysisObjectDTO hepatic,
        SepsisAnalysisObjectDTO hematologic,
        SepsisAnalysisObjectDTO metabolic
) {
}
