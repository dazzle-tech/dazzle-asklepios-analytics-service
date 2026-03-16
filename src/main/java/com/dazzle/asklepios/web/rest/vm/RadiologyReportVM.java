package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.enumeration.Gender;
import java.util.Date;

public record RadiologyReportVM(
        String facilityName,
        String departmentName,

        String patientFullName,
        String mrn,
        Date dateOfBirth,
        String age,
        Gender gender,
        String primaryMobileNumber,

        String encounterNumber,
        String orderingPhysician,
        String fromDepartment,
        String testName,

        String report,
        String severity,
        String approvedBy,
        String reviewedBy
) {}