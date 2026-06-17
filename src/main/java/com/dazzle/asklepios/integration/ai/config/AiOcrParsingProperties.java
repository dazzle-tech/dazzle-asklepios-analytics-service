package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.ocr-parsing")
public record AiOcrParsingProperties(
        String baseUrl,
        Integer timeoutSeconds
) {}
