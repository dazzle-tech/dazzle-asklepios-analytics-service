package com.dazzle.asklepios.service.dto.reports;

public record NurseSummaryAdditionalMeasurementsDTO(
        String ageGroup,
        String hearingTest,
        Boolean dehydration,
        Boolean nasalFlaring,
        Boolean responseToLight,
        Boolean pupilResponse,
        Boolean abilityToFollowTarget,
        Boolean colorTesting,
        Boolean fallRisk,
        Boolean visionProblemsAffectingFunction,
        Boolean hearingProblemsAffectingFunction,
        String details,
        String actionToTake
) {
}