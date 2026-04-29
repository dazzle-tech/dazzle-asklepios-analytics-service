package com.dazzle.asklepios.service.dto.reports;

import java.time.Instant;

public record NurseSummaryAllergyDTO(
        Long id,
        String allergenType,
        Long allergenId,
        String severity,
        String criticality,
        String certainty,
        String treatmentStrategy,
        String onset,
        Instant onsetDate,
        String typeOfPropensity,
        Boolean byPatient,
        String sourceOfInformation,
        String allergicReactions,
        String note,
        String status
) {
}