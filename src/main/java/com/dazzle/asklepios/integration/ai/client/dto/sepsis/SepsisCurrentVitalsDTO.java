package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

public record SepsisCurrentVitalsDTO(
        SepsisAnalysisObjectDTO HR,
        SepsisAnalysisObjectDTO Resp,
        SepsisAnalysisObjectDTO SBP,
        SepsisAnalysisObjectDTO DBP,
        SepsisAnalysisObjectDTO MAP,
        SepsisAnalysisObjectDTO O2Sat,
        SepsisAnalysisObjectDTO Temp,
        SepsisAnalysisObjectDTO EtCO2

) {
}
