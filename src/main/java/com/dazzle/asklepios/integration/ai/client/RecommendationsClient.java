package com.dazzle.asklepios.integration.ai.client;

import com.dazzle.asklepios.integration.ai.client.dto.recommendations.RecommendationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.RecommendationsResponseDTO;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.SpecialtyConsultationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.recommendations.SpecialtyConsultationResponseDTO;
import com.dazzle.asklepios.integration.ai.config.RecommendationsFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "recommendationsClient",
        url = "${ai.recommendations.base-url}",
        configuration = RecommendationsFeignConfig.class
)
public interface RecommendationsClient {

    @PostMapping("/recommendations")
    RecommendationsResponseDTO getRecommendations(RecommendationRequestDTO request);
    @PostMapping("/consultation/specialty")
    SpecialtyConsultationResponseDTO getSpecialtyConsultation(
            SpecialtyConsultationRequestDTO request
    );
}
