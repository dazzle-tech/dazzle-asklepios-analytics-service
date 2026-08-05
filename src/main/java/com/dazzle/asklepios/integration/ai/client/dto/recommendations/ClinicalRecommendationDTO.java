 package com.dazzle.asklepios.integration.ai.client.dto.recommendations;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClinicalRecommendationDTO(
        @JsonProperty("recommendation_id")
        String recommendationId,
        String type,
        String title,
        String description,
        String rationale,
        String priority,
        @JsonProperty("actionable_steps")
        List<String> actionableSteps,
        @JsonProperty("evidence_level")
        String evidenceLevel,
        List<String> contraindications,
        @JsonProperty("monitoring_requirements")
        String monitoringRequirements,
        @JsonProperty("follow_up")
        String followUp
) {}