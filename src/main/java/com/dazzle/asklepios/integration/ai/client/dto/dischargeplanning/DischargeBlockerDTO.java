package com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning;

public record DischargeBlockerDTO(
        String category,
        String title,
        String reason
) {}
