package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

public record SepsisCurrentLabsDTO(
        SepsisAnalysisObjectDTO Lactate,
        SepsisAnalysisObjectDTO WBC,
        SepsisAnalysisObjectDTO Platelets,
        SepsisAnalysisObjectDTO Creatinine,
        SepsisAnalysisObjectDTO Bilirubin_total,
        SepsisAnalysisObjectDTO HCO3,
        SepsisAnalysisObjectDTO BUN,
        SepsisAnalysisObjectDTO Hct
) {
}
