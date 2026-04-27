package com.dazzle.asklepios.service.dto.reports;

public record NurseSummaryServiceProductDTO(
        Long id,
        String category,
        Long serviceId,
        Long productId,
        Long quantity,
        String name,
        String code,
        String unit,
        String notes,
        String orderedAt
        ) {
}
