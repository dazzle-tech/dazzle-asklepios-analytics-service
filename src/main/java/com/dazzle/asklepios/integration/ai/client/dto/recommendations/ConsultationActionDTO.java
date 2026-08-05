package com.dazzle.asklepios.integration.ai.client.dto.recommendations;

public record ConsultationActionDTO(
        String title,
        String description,
        String category,
        String priority
) {}