package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.sepsis.SepsisRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.sepsis.SepsisResponseDTO;
import com.dazzle.asklepios.integration.ai.config.SepsisEarlyDetectionConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "SepsisEarlyDetectionClient", url = "${ai.sepsis-early-detection.base-url}", configuration = SepsisEarlyDetectionConfig.class)
public interface SepsisEarlyDetectionClient {

    @PostMapping("/analyses")
    SepsisResponseDTO analysis(SepsisRequestDTO request);
}