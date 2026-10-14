package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.clinical-summary-voice")
public record ClinicalSummaryVoiceProperties(
        String baseUrl,
        Integer timeoutSeconds
) {
}