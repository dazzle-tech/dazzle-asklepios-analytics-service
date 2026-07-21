package com.dazzle.asklepios.integration.ai.client.dto.autopopulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AutoPopulationResponseDTO(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("task_type") String taskType,
        @JsonProperty("output_language") String outputLanguage,
        @JsonProperty("structured_fields") StructuredFieldsDTO structuredFields,
        @JsonProperty("uncertainty_flags") List<UncertaintyFlagDTO> uncertaintyFlags,
        List<ContradictionFlagDTO> contradictions,
        @JsonProperty("source_trace") List<SourceTraceDTO> sourceTrace,
        List<WarningDTO> warnings,
        @JsonProperty("processing_metadata") Map<String, Object> processingMetadata
) {}
