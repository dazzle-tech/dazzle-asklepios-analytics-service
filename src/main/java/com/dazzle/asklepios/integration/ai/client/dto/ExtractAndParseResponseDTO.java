package com.dazzle.asklepios.integration.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtractAndParseResponseDTO(
        @JsonProperty("text_lines") List<String> textLines,
        @JsonProperty("structured_data") OCRParsingResponseDTO structuredData,
        String model
) {}
