package com.dazzle.asklepios.integration.ai.client.dto.lab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrendDTO(
        String lab_name,
        String direction,
        String summary,
        String from_value,
        String to_value,
        String from_timestamp,
        String to_timestamp,
        String unit
) {}
