package com.dazzle.asklepios.service.dto.reports;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record FinancialReportDTO(
        Long patientId,
        String medicalRecordNumber,
        String patientName,

        Long encounterId,
        String encounterNumber,
        LocalDateTime encounterDate,
        LocalTime encounterTime,
        String encounterType,
        String encounterStatus,

        String coverageType,

        String payerName,
        String policyNumber,
        String memberCardId,

        BigDecimal patientShare,
        BigDecimal insuranceShare,

        BigDecimal patientPaid,
        BigDecimal insurancePaid,

        BigDecimal patientOutstanding,
        BigDecimal insuranceOutstanding,

        BigDecimal copayAmount,
        BigDecimal deductibleAmount
) {}