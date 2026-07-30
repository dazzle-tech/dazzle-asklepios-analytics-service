package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

public record SepsisRecommendedActions(
        String priority,
        String action,
        String rationale
) {
}
