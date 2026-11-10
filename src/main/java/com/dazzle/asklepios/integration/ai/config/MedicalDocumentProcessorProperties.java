package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.medical-document-processor")
public record MedicalDocumentProcessorProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}
