package com.dazzle.asklepios.service.dto.reports;


public record NurseSummaryAllergyDTO(

        String type,
        String allergen,
        String severity
) {
}