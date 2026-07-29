package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.recommendations.RecommendationsResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.SpecialtyConsultationResponseDTO;
import com.dazzle.asklepios.integration.ai.controller.vm.PatientRecommendationRequestVM;
import com.dazzle.asklepios.integration.ai.controller.vm.PatientSpecialtyConsultationRequestVM;
import com.dazzle.asklepios.integration.ai.service.RecommendationsAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class RecommendationsAiController {

    private final RecommendationsAiService recommendationsAiService;

    @PostMapping("/recommendations")
    public ResponseEntity<RecommendationsResponseDTO> getRecommendations(
            @RequestBody PatientRecommendationRequestVM request
    ) {
        return ResponseEntity.ok(
                recommendationsAiService.getRecommendations(request)
        );
    }

    @PostMapping("/consultation/specialty")
    public ResponseEntity<SpecialtyConsultationResponseDTO> getSpecialtyConsultation(
            @RequestBody PatientSpecialtyConsultationRequestVM request
    ) {
        return ResponseEntity.ok(
                recommendationsAiService.getSpecialtyConsultation(request)
        );
    }
}