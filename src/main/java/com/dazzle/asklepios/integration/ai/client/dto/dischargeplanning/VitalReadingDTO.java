package com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning;

public record VitalReadingDTO(
        String name,
        String value,
        String unit,
        String timestamp
) {}
