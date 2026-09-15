package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.discharge-report")
public record DischargeReportProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}
