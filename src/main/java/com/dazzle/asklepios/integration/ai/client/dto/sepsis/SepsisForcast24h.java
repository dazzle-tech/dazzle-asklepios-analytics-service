package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SepsisForcast24h(
        @JsonProperty("expected_trajectory") String expectedTrajectory,
        String narrative,
        @JsonProperty("key_decision_points") List<SepsisKeyDecisionPoints> keyDecisionPoints,
        @JsonProperty("intervention_urgency") String interventionUrgency
) {
}
