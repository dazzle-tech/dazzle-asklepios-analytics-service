package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.autopopulation.AutoPopulationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.autopopulation.AutoPopulationResponseDTO;
import com.dazzle.asklepios.integration.ai.config.AutoPopulationFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "autoPopulationClient",
        url = "${ai.auto-population.base-url}",
        configuration = AutoPopulationFeignConfig.class
)
public interface AutoPopulationClient {

    @PostMapping("/auto-populate")
    AutoPopulationResponseDTO autoPopulate(@RequestBody AutoPopulationRequestDTO request);
}
