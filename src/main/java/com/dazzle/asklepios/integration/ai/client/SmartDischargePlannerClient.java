package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.DischargePlanningRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.DischargePlanningResponseDTO;
import com.dazzle.asklepios.integration.ai.config.SmartDischargePlannerFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "smartDischargePlannerClient",
        url = "${ai.smart-discharge-planner.base-url}",
        configuration = SmartDischargePlannerFeignConfig.class
)
public interface SmartDischargePlannerClient {

    @PostMapping("/api/v1/plan-discharge")
    DischargePlanningResponseDTO planDischarge(@RequestBody DischargePlanningRequestDTO request);
}
