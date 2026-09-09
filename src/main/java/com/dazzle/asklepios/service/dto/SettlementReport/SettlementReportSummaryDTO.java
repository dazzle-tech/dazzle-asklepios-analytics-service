package com.dazzle.asklepios.service.dto.SettlementReport;
import java.math.BigDecimal;

public record SettlementReportSummaryDTO(

        BigDecimal totalBilledAmount,
        BigDecimal totalApprovedAmount,
        BigDecimal totalRejectedAmount,

        BigDecimal totalPatientShare,
        BigDecimal totalInsuranceAmount,

        BigDecimal totalPaidAmount,
        BigDecimal totalOutstandingAmount,

        Long totalSettlements

) {
}

