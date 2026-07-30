package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.sepsis.SepsisResponseDTO;
import com.dazzle.asklepios.integration.ai.controller.vm.SepsisRequestVM;
import com.dazzle.asklepios.integration.ai.service.SepsisEarlyDetectionAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class SepsisEarlyDetectionController {
    private final SepsisEarlyDetectionAiService sepsisEarlyDetectionAiService;

    @PostMapping("/sepsis-early-detection/analyse")
    public ResponseEntity<SepsisResponseDTO> getClinicalSummary(@RequestBody SepsisRequestVM request) {
        return ResponseEntity.ok(sepsisEarlyDetectionAiService.analyse(request));
    }
}
