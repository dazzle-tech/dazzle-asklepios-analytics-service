package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.medication-test-orders-validation")
public record MedicationTestOrdersValidationProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}
