package com.dazzle.asklepios.service.dto.reports;

import jakarta.persistence.Column;

import java.time.Instant;
import java.time.LocalDate;

public record NurseSummaryEncounterInfoDTO(
        Long encounterId,
        String encounterNumber,
        LocalDate encounterDate,
        String encounterReason,
        String priority,
        String status,
        String chiefComplaint,
        String facilityName,
        String departmentName,
        Instant createdDate,
        String historyOfPresentIllness,
        String physicalExaminationSummery
) {
}