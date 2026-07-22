package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.clinical-summary")
public record AiClinicalSummaryProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}