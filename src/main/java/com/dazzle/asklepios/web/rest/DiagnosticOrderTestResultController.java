package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.DiagnosticOrderTestResultReportService;
import com.dazzle.asklepios.service.LaboratoryPdfRenderService;
import com.dazzle.asklepios.service.dto.LaboratoryResultReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/laboratory-reports/result/{resultId}")
    public ResponseEntity<LaboratoryResultReportDTO> getLaboratoryResult(@PathVariable Long resultId) {

        LOG.debug("[LaboratoryResultReportResource] GET_LABORATORY_RESULT - start. resultId={}", resultId);

        LaboratoryResultReportDTO report = resultReportService.getLaboratoryResult(resultId);

        LOG.debug("[LaboratoryResultReportResource] GET_LABORATORY_RESULT - completed. resultId={}", resultId);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/laboratory-reports/result/{id}/pdf")
    public ResponseEntity<byte[]> generateLaboratoryPdf(@PathVariable Long id) {
        byte[] pdf = laboratoryPdfRenderService.generateLaboratoryPdf(id);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=laboratory-result-" + id + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
}