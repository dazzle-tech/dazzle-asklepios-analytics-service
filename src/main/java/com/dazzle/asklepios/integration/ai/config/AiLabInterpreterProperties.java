package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.lab-interpreter")
public record AiLabInterpreterProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}