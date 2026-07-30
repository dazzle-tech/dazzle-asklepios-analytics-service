package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.sepsis-early-detection")
public record SepsisEarlyDetectionProperties(
        String baseUrl,
        Integer timeoutSeconds
) {
}
