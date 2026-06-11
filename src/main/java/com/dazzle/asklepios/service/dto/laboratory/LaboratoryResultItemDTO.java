package com.dazzle.asklepios.service.dto.laboratory;

import com.dazzle.asklepios.domain.enumeration.diagnostictest.TestResultMarker;

import java.time.Instant;

public record LaboratoryResultItemDTO(
        Instant resultDate,
        String normalRange,
        String categoryName,
        String testName,

        String result,
        String unit,
        TestResultMarker marker,
        Instant reviewedDate,
        String reviewedBy
) {
}