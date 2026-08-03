package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

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
