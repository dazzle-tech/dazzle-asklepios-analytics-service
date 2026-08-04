package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.quality-discharge-report")
public record QualityDischargeReportProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}
