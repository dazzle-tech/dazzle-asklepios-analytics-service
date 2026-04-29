package com.dazzle.asklepios.service.dto.reports;

import com.dazzle.asklepios.domain.enumeration.Severity;

public record NurseSummaryAllergyDTO(
        Long id,
        String allergenType,
        String allergen,
        Severity severity
) {
}