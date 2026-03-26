package com.dazzle.asklepios.service.dto.reports;

public record NurseSummaryObservationDTO(
        String reasonOfVisit,
        String functionalStatus,
        String patientConditions,
        String cognitiveCheck,
        String primaryDiagnosis,
        String plan
) {
}