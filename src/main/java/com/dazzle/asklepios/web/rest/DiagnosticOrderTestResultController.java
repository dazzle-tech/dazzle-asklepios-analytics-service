package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.DiagnosticOrderTestResultReportService;
import com.dazzle.asklepios.service.LaboratoryPdfRenderService;
import com.dazzle.asklepios.service.dto.laboratory.LaboratoryResultReportDTO;
import com.dazzle.asklepios.service.dto.reports.DiagnosticResultReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class DiagnosticOrderTestResultController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderTestResultController.class);

    private final DiagnosticOrderTestResultReportService resultReportService;
    private final LaboratoryPdfRenderService laboratoryPdfRenderService;

    public DiagnosticOrderTestResultController(
            DiagnosticOrderTestResultReportService resultReportService,
            LaboratoryPdfRenderService laboratoryPdfRenderService
    ) {
        this.resultReportService = resultReportService;
        this.laboratoryPdfRenderService = laboratoryPdfRenderService;
    }

    @GetMapping("/laboratory-reports/results")
    public ResponseEntity<LaboratoryResultReportDTO> getLaboratoryResults(
            @RequestParam(required = false) String timezone,
            @RequestParam List<Long> resultIds
    ) {
        LOG.debug("[LaboratoryResultReportResource] GET_LABORATORY_RESULTS - start. resultIds={}", resultIds);

        LaboratoryResultReportDTO report = resultReportService.getLaboratoryResults(resultIds,timezone);

        LOG.debug("[LaboratoryResultReportResource] GET_LABORATORY_RESULTS - completed. resultIds={}", resultIds);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/laboratory-reports/results/pdf")
    public ResponseEntity<byte[]> generateLaboratoryPdf(
            @RequestParam List<Long> resultIds,
            @RequestParam(required = false) String timezone,
            @RequestParam(defaultValue = "en") String lang
    ) {
        LOG.debug("[LaboratoryResultReportResource] GENERATE_LABORATORY_RESULTS_PDF - start. resultIds={}, lang={}", resultIds, lang);

        byte[] pdf = laboratoryPdfRenderService.generateLaboratoryPdf(resultIds, lang,timezone);

        LOG.debug("[LaboratoryResultReportResource] GENERATE_LABORATORY_RESULTS_PDF - completed. resultIds={}", resultIds);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=laboratory-results.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }

    @GetMapping("/laboratory-reports/laboratory-results")
    public ResponseEntity<List<DiagnosticResultReportDTO>> getLaboratoryResults(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ResponseEntity.ok(
                resultReportService.getLaboratoryResults(
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/radiology-reports/radiology-results")
    public ResponseEntity<List<DiagnosticResultReportDTO>> getRadiologyResults(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ResponseEntity.ok(
                resultReportService.getRadiologyResults(
                        startDate,
                        endDate
                )
        );
    }
}