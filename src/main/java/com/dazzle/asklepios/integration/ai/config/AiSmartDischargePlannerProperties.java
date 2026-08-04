package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.smart-discharge-planner")
public record AiSmartDischargePlannerProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}
