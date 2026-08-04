package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning.DischargePlanningResponseDTO;
import com.dazzle.asklepios.integration.ai.service.SmartDischargePlannerIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class SmartDischargePlannerController {

    private final SmartDischargePlannerIntegrationService smartDischargePlannerIntegrationService;

    @PostMapping("/encounters/{encounterId}/discharge-readiness")
    public ResponseEntity<DischargePlanningResponseDTO> assessDischargeReadiness(@PathVariable Long encounterId) {
        return ResponseEntity.ok(
                smartDischargePlannerIntegrationService.assessDischargeReadiness(encounterId)
        );
    }
}
