package com.dazzle.asklepios.integration.ai.client.dto.lab;

import java.time.Instant;

public record LabResultDTO(
        String name,
        String value,
        String unit,
        String reference_range,
        String flag,
        Instant timestamp
) {}