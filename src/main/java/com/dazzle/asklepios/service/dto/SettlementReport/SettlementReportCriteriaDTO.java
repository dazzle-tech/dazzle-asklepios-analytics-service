package com.dazzle.asklepios.service.dto.SettlementReport;

import java.time.LocalDate;

public record SettlementReportCriteriaDTO(
        Long insuranceCompanyId,
        LocalDate settlementDateFrom,
        LocalDate settlementDateTo,
        String encounterType,
        String insuranceCompanyName

) {
}

