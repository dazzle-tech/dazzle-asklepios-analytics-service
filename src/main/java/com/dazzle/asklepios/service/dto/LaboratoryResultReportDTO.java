package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.domain.enumeration.diagnostictest.TestResultMarker;

import java.time.Instant;
import java.util.Date;

public record LaboratoryResultReportDTO(
        String facilityName,
        String departmentName,

        String patientFullName,
        String mrn,
        Date dateOfBirth,
        String age,
        Gender gender,
        String primaryMobileNumber,

        String encounterNumber,
        Long orderNumber,
        Instant resultDate,
        String normalRange,
        String fromDepartment,
        String testName,

        String result,
        String unit,
        TestResultMarker marker,
        Instant reviewedDate
) {
}