package com.dazzle.asklepios.integration.ai.client.dto.medvalidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ValidationResponseDTO(
        @JsonProperty("quick_summary") QuickSummaryDTO quickSummary,
        @JsonProperty("detailed_validations") List<DetailedValidationDTO> detailedValidations,
        @JsonProperty("recommended_alternatives") List<RecommendedAlternativeDTO> recommendedAlternatives,
        @JsonProperty("confidence_score") Double confidenceScore,
        String timestamp
) {}
