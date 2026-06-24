package com.dazzle.asklepios.integration.ai.client.dto.medvalidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuickSummaryDTO(
        @JsonProperty("overall_status") String overallStatus,
        @JsonProperty("top_priority") String topPriority
) {}
