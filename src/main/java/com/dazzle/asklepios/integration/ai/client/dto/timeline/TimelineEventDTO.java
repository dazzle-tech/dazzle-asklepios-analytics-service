package com.dazzle.asklepios.integration.ai.client.dto.timeline;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TimelineEventDTO(
        String date,

        @JsonProperty("event_type")
        String eventType,

        String title,
        String description,

        @JsonProperty("clinical_importance")
        String clinicalImportance,

        String source
) {}
