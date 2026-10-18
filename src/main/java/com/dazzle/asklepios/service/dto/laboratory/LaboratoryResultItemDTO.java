package com.dazzle.asklepios.service.dto.laboratory;


public record LaboratoryResultItemDTO(
        String resultDate,
        String normalRange,
        String categoryName,
        String testName,

        String result,
        String unit,
        String marker,
        String reviewedDate,
        String reviewedBy
) {
}