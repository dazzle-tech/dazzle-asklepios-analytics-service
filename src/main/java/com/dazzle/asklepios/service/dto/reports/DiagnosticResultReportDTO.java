package com.dazzle.asklepios.service.dto.reports;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiagnosticResultReportDTO(

        String firstName,
        String lastName,
        String medicalRecordNumber,
        String encounterNumber,

        LocalDateTime visitDate,

        String departmentName,

        String practitionerFirstName,
        String practitionerLastName,

        String testName,
        String profileName,

        LocalDateTime testOrderedDate,

        String testType,

        String receivedDepartmentName,

        String testStatus,
        String orderStatus,

        BigDecimal resultValueNumber,
        String resultValueText,
        String resultNormalRangeValue,
        String resultType,
        String resultUnit,

        LocalDateTime resultValueDate,

        String resultApprovedBy,
        LocalDateTime resultApprovedDate,

        String resultStatus
) {}