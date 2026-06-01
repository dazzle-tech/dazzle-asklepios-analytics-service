package com.dazzle.asklepios.service.dto.reports;

import java.time.Instant;

public record NurseSummaryWarningDTO(
        String warningType,
        String warning,
        String severity,
        Instant onsetDate,
        Boolean byPatient,
        String sourceOfInformation,
        String note,
        String actionTaken,
        String status
) {
}