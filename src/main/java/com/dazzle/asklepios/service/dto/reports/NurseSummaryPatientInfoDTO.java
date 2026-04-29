package com.dazzle.asklepios.service.dto.reports;

import java.time.Instant;
import java.util.Date;

public record NurseSummaryPatientInfoDTO(
        Long patientId,
        String fullName,
        String medicalRecordNumber,
        Date dateOfBirth,
        String age,
        String gender
) {
}