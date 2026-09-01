package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.lab.LabInterpretationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.lab.LabInterpretationResponseDTO;
import com.dazzle.asklepios.integration.ai.config.RecommendationsFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "labInterpreterClient",
        url = "${ai.lab-interpreter.base-url}",
        configuration = RecommendationsFeignConfig.class
)
public interface LabInterpreterClient {

    @PostMapping("/interpret-labs")
    LabInterpretationResponseDTO interpret(LabInterpretationRequestDTO request);
}