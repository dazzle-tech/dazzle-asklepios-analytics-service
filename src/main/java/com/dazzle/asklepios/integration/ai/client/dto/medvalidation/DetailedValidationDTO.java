package com.dazzle.asklepios.integration.ai.client.dto.medvalidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DetailedValidationDTO(
        @JsonProperty("item") String item,
        @JsonProperty("severity") String severity,
        @JsonProperty("issue") String issue,
        @JsonProperty("recommendation") String recommendation,
        @JsonProperty("evidence") String evidence
) {}
