package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.summary.SummaryResponseDTO;
import com.dazzle.asklepios.integration.ai.controller.vm.PatientClinicalSummaryRequestVM;
import com.dazzle.asklepios.integration.ai.service.ClinicalSummaryAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class SummarizationController {
    private final ClinicalSummaryAiService clinicalSummaryAiService;
    @PostMapping("/clinical-summary")
    public ResponseEntity<SummaryResponseDTO> getClinicalSummary(
            @RequestBody PatientClinicalSummaryRequestVM request
    ) {
        return ResponseEntity.ok(
                clinicalSummaryAiService.getClinicalSummary(request)
        );
    }
}