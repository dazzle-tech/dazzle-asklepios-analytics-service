package com.dazzle.asklepios.integration.ai.client.dto.recommendations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RecommendationsResponseDTO(
        @JsonProperty("request_id")
        String requestId,

        @JsonProperty("patient_id")
        String patientId,

        List<ClinicalRecommendationDTO> recommendations,

        String summary,

        @JsonProperty("total_recommendations")
        Integer totalRecommendations,

        @JsonProperty("priority_breakdown")
        Map<String, Integer> priorityBreakdown,

        @JsonProperty("processing_metadata")
        Map<String, Object> processingMetadata
) {}