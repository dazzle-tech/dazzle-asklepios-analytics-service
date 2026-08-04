package com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record DischargePlanningResponseDTO(
        @JsonProperty("request_id")
        String requestId,

        @JsonProperty("discharge_plan")
        DischargePlanDTO dischargePlan,

        String summary,

        @JsonProperty("processing_metadata")
        Map<String, Object> processingMetadata
) {}
