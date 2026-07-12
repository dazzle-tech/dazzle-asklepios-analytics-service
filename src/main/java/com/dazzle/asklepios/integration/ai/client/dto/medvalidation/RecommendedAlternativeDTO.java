package com.dazzle.asklepios.integration.ai.client.dto.medvalidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RecommendedAlternativeDTO(
        @JsonProperty("original_item") String originalItem,
        @JsonProperty("alternative") String alternative,
        @JsonProperty("rationale") String rationale
) {}
