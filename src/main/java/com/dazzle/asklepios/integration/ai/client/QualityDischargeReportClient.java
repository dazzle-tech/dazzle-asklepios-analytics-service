package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.discharge.QualityDischargeReportRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.discharge.QualityDischargeReportResponseDTO;
import com.dazzle.asklepios.integration.ai.config.QualityDischargeReportFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "qualityDischargeReportClient",
        url = "${ai.quality-discharge-report.base-url}",
        configuration = QualityDischargeReportFeignConfig.class
)
public interface QualityDischargeReportClient {

    @PostMapping("/discharge/qa/direct")
    QualityDischargeReportResponseDTO performQA(@RequestBody QualityDischargeReportRequestDTO request);
}
