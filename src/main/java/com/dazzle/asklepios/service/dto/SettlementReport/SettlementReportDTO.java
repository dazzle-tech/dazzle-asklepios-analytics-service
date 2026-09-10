package com.dazzle.asklepios.service.dto.SettlementReport;

import java.time.Instant;
import java.util.List;

public record SettlementReportDTO(

        SettlementReportCriteriaDTO criteria,

        List<SettlementReportRowDTO> settlements,

        SettlementReportSummaryDTO summary,

        Instant generatedAt

) {
}