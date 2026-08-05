package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.radiology-template")
public record AiRadiologyTemplateProperties(
        String baseUrl,
        Integer timeoutSeconds
) {
}