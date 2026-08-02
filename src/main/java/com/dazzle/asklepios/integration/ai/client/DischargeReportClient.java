package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.discharge.DischargeReportRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.discharge.DischargeReportResponseDTO;
import com.dazzle.asklepios.integration.ai.config.DischargeReportFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "dischargeReportClient",
        url = "${ai.discharge-report.base-url}",
        configuration = DischargeReportFeignConfig.class
)
public interface DischargeReportClient {

    @PostMapping("/discharge/generate-report")
    DischargeReportResponseDTO generateReport(@RequestBody DischargeReportRequestDTO request);
}
