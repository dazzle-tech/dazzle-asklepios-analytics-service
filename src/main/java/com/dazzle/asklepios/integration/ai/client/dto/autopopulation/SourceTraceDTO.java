package com.dazzle.asklepios.integration.ai.client.dto.autopopulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SourceTraceDTO(
        @JsonProperty("field_name") String fieldName,
        String source,
        @JsonProperty("extraction_method") String extractionMethod,
        String timestamp
) {}
