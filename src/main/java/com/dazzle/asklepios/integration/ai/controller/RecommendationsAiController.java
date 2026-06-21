package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.recommendations.RecommendationsResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.RecommendationRequestDTO;
import com.dazzle.asklepios.integration.ai.controller.dto.PatientRecommendationRequestVM;
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
}