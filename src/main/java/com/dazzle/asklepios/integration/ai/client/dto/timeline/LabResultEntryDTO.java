package com.dazzle.asklepios.integration.ai.client.dto.timeline;

public record LabResultEntryDTO(
        String name,
        String value,
        String unit,
        String date,
        String flag
) {}
