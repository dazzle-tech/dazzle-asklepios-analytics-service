package com.dazzle.asklepios.service.dto.reports;

import java.time.Instant;

public record NurseSummaryPatientInfoDTO(
        Long patientId,
        String fullName,
        String medicalRecordNumber,
        Instant dateOfBirth,
        Integer age,
        String gender
) {
}