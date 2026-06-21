package com.dazzle.asklepios.integration.ai.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.recommendations")
public record AiRecommendationsProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}