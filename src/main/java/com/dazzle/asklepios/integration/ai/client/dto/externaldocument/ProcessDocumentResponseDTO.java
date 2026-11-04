package com.dazzle.asklepios.integration.ai.client.dto.externaldocument;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

// Reads the AI snake_case fields, but returns camelCase to the UI like our other endpoints
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProcessDocumentResponseDTO(
        String status,
        String reason,
        @JsonAlias("detected_language") String detectedLanguage,
        Boolean translated,
        @JsonAlias("document_type") String documentType,
        @JsonAlias("structured_data") Map<String, Object> structuredData,
        @JsonAlias("raw_extracted_text") String rawExtractedText,
        @JsonAlias("request_id") String requestId,
        @JsonAlias("processing_metadata") Map<String, Object> processingMetadata
) {}
