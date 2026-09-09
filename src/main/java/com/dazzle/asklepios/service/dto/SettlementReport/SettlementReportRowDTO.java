package com.dazzle.asklepios.service.dto.SettlementReport;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SettlementReportRowDTO(

        String patientName,
        String patientId,

        String invoiceNumber,

        String settlementNumber,
        LocalDate settlementDate,

        String insuranceCompany,

        String claimNumber,
        LocalDate claimDate,

        BigDecimal billedAmount,
        BigDecimal approvedAmount,
        BigDecimal rejectedAmount,

        BigDecimal patientShare,
        BigDecimal insuranceAmount,

        BigDecimal paidAmount,
        BigDecimal outstandingAmount,

        String settlementStatus
) {
}
