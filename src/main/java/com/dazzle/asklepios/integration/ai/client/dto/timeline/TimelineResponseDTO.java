package com.dazzle.asklepios.integration.ai.client.dto.timeline;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TimelineResponseDTO(
        @JsonProperty("request_id")
        String requestId,

        List<TimelineEventDTO> timeline,
        String summary,

        @JsonProperty("processing_metadata")
        ProcessingMetadataDTO processingMetadata
) {}
