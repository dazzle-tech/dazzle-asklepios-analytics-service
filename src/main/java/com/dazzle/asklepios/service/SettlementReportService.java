package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.SettlementReport.SettlementReportDTO;
import com.dazzle.asklepios.service.dto.SettlementReport.SettlementReportRequestDTO;
import com.dazzle.asklepios.service.dto.SettlementReport.SettlementReportRowDTO;
import com.dazzle.asklepios.service.dto.SettlementReport.SettlementReportSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementReportService {

    public SettlementReportDTO getSettlementReport(
            SettlementReportRequestDTO request
    ) {

        SettlementReportSummaryDTO summary =
                buildSummary(request.rows());

        return new SettlementReportDTO(
                request.criteria(),
                request.rows(),
                summary,
                Instant.now()
        );
    }

    private SettlementReportSummaryDTO buildSummary(
            List<SettlementReportRowDTO> rows
    ) {

        return new SettlementReportSummaryDTO(

                sum(rows.stream()
                        .map(SettlementReportRowDTO::billedAmount)
                        .toList()),

                sum(rows.stream()
                        .map(SettlementReportRowDTO::approvedAmount)
                        .toList()),

                sum(rows.stream()
                        .map(SettlementReportRowDTO::rejectedAmount)
                        .toList()),

                sum(rows.stream()
                        .map(SettlementReportRowDTO::patientShare)
                        .toList()),

                sum(rows.stream()
                        .map(SettlementReportRowDTO::insuranceAmount)
                        .toList()),

                sum(rows.stream()
                        .map(SettlementReportRowDTO::paidAmount)
                        .toList()),

                sum(rows.stream()
                        .map(SettlementReportRowDTO::outstandingAmount)
                        .toList()),

                (long) rows.size()
        );
    }

    private BigDecimal sum(List<BigDecimal> amounts) {

        return amounts.stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}