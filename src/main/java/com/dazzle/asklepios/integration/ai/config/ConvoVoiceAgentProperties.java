package com.dazzle.asklepios.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.convo-voice-agent")
public record ConvoVoiceAgentProperties(
        String baseUrl,
        Integer timeoutSeconds
) {
}
