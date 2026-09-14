package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.patient-timeline")
public record PatientTimelineProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}
