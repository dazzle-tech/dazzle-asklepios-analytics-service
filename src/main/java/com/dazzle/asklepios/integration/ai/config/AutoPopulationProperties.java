package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.auto-population")
public record AutoPopulationProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}
