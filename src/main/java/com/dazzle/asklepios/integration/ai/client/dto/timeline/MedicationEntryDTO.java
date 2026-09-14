package com.dazzle.asklepios.integration.ai.client.dto.timeline;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MedicationEntryDTO(
        String name,

        @JsonProperty("start_date")
        String startDate,

        @JsonProperty("end_date")
        String endDate,

        String status
) {}
