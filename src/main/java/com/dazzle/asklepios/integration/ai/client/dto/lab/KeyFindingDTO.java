package com.dazzle.asklepios.integration.ai.client.dto.lab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeyFindingDTO(
        String lab_name,
        String value,
        String unit,
        String flag,
        String finding
) {}
