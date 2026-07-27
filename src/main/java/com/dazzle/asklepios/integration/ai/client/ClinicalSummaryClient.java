package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.summary.SummaryRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.summary.SummaryResponseDTO;
import com.dazzle.asklepios.integration.ai.config.RecommendationsFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "clinicalSummaryClient",
        url = "${ai.clinical-summary.base-url}",
        configuration = RecommendationsFeignConfig.class
)
public interface ClinicalSummaryClient {

    @PostMapping("api/v1/summarize")
    SummaryResponseDTO summarize(SummaryRequestDTO request);
}