package com.dazzle.asklepios.service.dto.reports;

import com.dazzle.asklepios.domain.enumeration.AllergenTypes;

public record NurseSummaryAllergyDTO(

        AllergenTypes type,
        String allergen,
        String severity
) {
}