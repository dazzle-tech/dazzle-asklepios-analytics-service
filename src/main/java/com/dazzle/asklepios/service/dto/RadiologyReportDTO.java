package com.dazzle.asklepios.service.dto;

import java.util.Date;

public record RadiologyReportDTO(
        String facilityName,
        String departmentName,

        String patientFullName,
        String mrn,
        Date dateOfBirth,
        String age,
        String gender,
        String primaryMobileNumber,

        String encounterNumber,
        String orderingPhysician,
        String fromDepartment,
        String testName,

        String report,
        String severity,
       String criticalFindings,
       String radiologistComments,
        String radiologistInformation,
        String approvedBy,
        String reviewedBy
) {}