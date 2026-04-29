package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryServiceProductDTO;
import com.dazzle.asklepios.service.dto.reports.VisitReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitReportService {

    private final NurseSummaryReportService nurseSummaryReportService;

    public VisitReportDTO getVisitReport(Long encounterId) {

        NurseSummaryReportDTO nurseSummary =
                nurseSummaryReportService.getNurseSummaryReport(encounterId);

        if (nurseSummary == null) {
            return null;
        }

        List<NurseSummaryServiceProductDTO> diagnostics =
                nurseSummary.servicesAndProducts() == null ? List.of() :
                        nurseSummary.servicesAndProducts().stream()
                                .toList();

        List<NurseSummaryServiceProductDTO> medications =
                nurseSummary.servicesAndProducts() == null ? List.of() :
                        nurseSummary.servicesAndProducts().stream()
                                .toList();

        List<NurseSummaryServiceProductDTO> procedures =
                nurseSummary.servicesAndProducts() == null ? List.of() :
                        nurseSummary.servicesAndProducts().stream()
                                .toList();


        return new VisitReportDTO(
                nurseSummary.patientInfo(),
                nurseSummary.encounterInfo(),
                nurseSummary.observation(),
                nurseSummary.vitalSigns(),
                nurseSummary.bodyMeasurements(),
                nurseSummary.additionalMeasurements(),
                nurseSummary.allergies(),
                nurseSummary.warnings(),
                diagnostics,
                medications,
                procedures,
                null,
                Instant.now()
        );
    }
}