package com.dazzle.asklepios.service.dto.reports;

import java.time.Instant;
import java.time.LocalDate;

public record NurseSummaryEncounterInfoDTO(
        Long encounterId,
        String encounterNumber,
        LocalDate encounterDate,
        String encounterType,
        String encounterReason,
        String priority,
        String status,
        String chiefComplaint,
        Long facilityId,
        Long departmentId,
        Instant createdDate
) {
}