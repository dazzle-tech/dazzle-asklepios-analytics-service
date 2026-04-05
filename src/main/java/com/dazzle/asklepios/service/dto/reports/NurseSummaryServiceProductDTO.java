package com.dazzle.asklepios.service.dto.reports;

public record NurseSummaryServiceProductDTO(
        Long id,
        String category,
        Long quantity,
        Long entityQuantity, String notes
) {}