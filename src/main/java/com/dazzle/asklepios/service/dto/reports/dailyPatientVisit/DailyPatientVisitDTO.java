package com.dazzle.asklepios.service.dto.reports.dailyPatientVisit;

import java.time.Instant;
import java.util.Date;


public record DailyPatientVisitDTO(

        String firstName,
        String lastName,
        String medicalRecordNumber,
        String encounterNumber,
        Instant visitDate,
        String departmentName,
        String practitionerFirstName,
        String practitionerLastName,
        Date dateOfBirth,
        String gender,
        String status,
        String encounterStatus
) {
}
