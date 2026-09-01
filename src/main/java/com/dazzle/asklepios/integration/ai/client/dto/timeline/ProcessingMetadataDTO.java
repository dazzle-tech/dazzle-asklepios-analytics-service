package com.dazzle.asklepios.integration.ai.client.dto.timeline;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessingMetadataDTO(
        String model,
        String timestamp,

        @JsonProperty("input_fields_count")
        Integer inputFieldsCount,

        @JsonProperty("timeline_event_count")
        Integer timelineEventCount
) {}
