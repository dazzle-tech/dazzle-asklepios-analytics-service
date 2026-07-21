package com.dazzle.asklepios.integration.ai.client.dto.autopopulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WarningDTO(
        String level,
        String message,
        @JsonProperty("field_name") String fieldName
) {}
