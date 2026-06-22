package com.dazzle.asklepios.integration.ai.client.dto.summary;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record SummaryResponseDTO(
        @JsonProperty("request_id")
        String requestId,

        @JsonProperty("ClinicalSummary")
        String clinicalSummary,

        @JsonProperty("processing_metadata")
        Map<String, Object> processingMetadata
) {}