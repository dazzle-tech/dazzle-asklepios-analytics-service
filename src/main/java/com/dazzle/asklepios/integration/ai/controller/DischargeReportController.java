package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.discharge.DischargeReportQaRequestVM;
import com.dazzle.asklepios.integration.ai.client.dto.discharge.DischargeReportResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.discharge.QualityDischargeReportResponseDTO;
import com.dazzle.asklepios.integration.ai.service.DischargeReportIntegrationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class DischargeReportController {

    private static final Logger LOG = LoggerFactory.getLogger(DischargeReportController.class);

    private final DischargeReportIntegrationService dischargeReportIntegrationService;

    @GetMapping("/encounters/{encounterId}/discharge-report")
    public ResponseEntity<DischargeReportResponseDTO> getDischargeReport(@PathVariable Long encounterId) {
        LOG.debug("[REST][DISCHARGE_REPORT][GET] encounterId={}", encounterId);
        return ResponseEntity.ok(dischargeReportIntegrationService.generateReport(encounterId));
    }

    @PostMapping("/encounters/{encounterId}/discharge-report/qa")
    public ResponseEntity<QualityDischargeReportResponseDTO> getDischargeReportQualityCheck(
            @PathVariable Long encounterId,
            @RequestBody DischargeReportQaRequestVM body
    ) {
        LOG.debug("[REST][DISCHARGE_REPORT_QA][POST] encounterId={}", encounterId);
        return ResponseEntity.ok(
                dischargeReportIntegrationService.performQualityCheck(encounterId, body.dischargeReport())
        );
    }
}
