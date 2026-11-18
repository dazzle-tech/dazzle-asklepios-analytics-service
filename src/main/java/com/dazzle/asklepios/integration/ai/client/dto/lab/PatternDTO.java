package com.dazzle.asklepios.integration.ai.client.dto.lab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PatternDTO(
        String label,
        String reason
) {}
