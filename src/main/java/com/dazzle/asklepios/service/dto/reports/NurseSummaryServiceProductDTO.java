package com.dazzle.asklepios.service.dto.reports;

public record NurseSummaryServiceProductDTO(
        Long id,
        Long serviceId,

        Long quantity,
        String name,
        String code,
        String unit,
        String notes,
        String orderedAt
        ) {
}
