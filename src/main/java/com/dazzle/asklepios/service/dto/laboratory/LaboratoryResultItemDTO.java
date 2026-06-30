package com.dazzle.asklepios.service.dto.laboratory;

import com.dazzle.asklepios.domain.enumeration.diagnostictest.TestResultMarker;

public record LaboratoryResultItemDTO(
        String resultDate,
        String normalRange,
        String categoryName,
        String testName,

        String result,
        String unit,
        TestResultMarker marker,
        String reviewedDate,
        String reviewedBy
) {
}