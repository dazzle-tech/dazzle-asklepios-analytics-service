package com.dazzle.asklepios.service.dto.SettlementReport;
import java.util.List;

public record SettlementReportRequestDTO(

        SettlementReportCriteriaDTO criteria,

        List<SettlementReportRowDTO> rows

) {
}
