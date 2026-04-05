package com.dazzle.asklepios.service.dto.reports;

public record NurseSummaryServiceProductDTO(
        Long id,
        String category,
        Long quantity,
        String code,
        String notes,
        String orderedAt
) {}