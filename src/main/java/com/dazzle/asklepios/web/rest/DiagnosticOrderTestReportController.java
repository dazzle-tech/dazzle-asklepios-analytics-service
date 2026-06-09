package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.DiagnosticOrderTestReportService;
import com.dazzle.asklepios.service.RadiologyPdfRenderService;
import com.dazzle.asklepios.service.dto.RadiologyReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/analytics")
public class DiagnosticOrderTestReportController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderTestReportController.class);

    private final DiagnosticOrderTestReportService reportService;
    private final RadiologyPdfRenderService radiologyPdfRenderService;

    public DiagnosticOrderTestReportController(DiagnosticOrderTestReportService reportService, RadiologyPdfRenderService radiologyPdfRenderService) {
        this.reportService = reportService;

        this.radiologyPdfRenderService = radiologyPdfRenderService;
    }

    @GetMapping("/radiology-reports/{reportId}")
    public ResponseEntity<RadiologyReportDTO> getRadiologyReport(@PathVariable Long reportId) {

        LOG.debug("[RadiologyReportResource] GET_RADIOLOGY_REPORT - start. reportId={}", reportId);

        RadiologyReportDTO report = reportService.getRadiologyReport(reportId);

        LOG.debug("[RadiologyReportResource] GET_RADIOLOGY_REPORT - completed. reportId={}", reportId);

        return ResponseEntity.ok(report);
    }
    @GetMapping("/radiology-reports/{id}/pdf")
    public ResponseEntity<byte[]> generateRadiologyPdf(@PathVariable Long id ,
                                                       @RequestParam (defaultValue = "en")String lang) {
        byte[] pdf = radiologyPdfRenderService.generateRadiologyPdf(id,lang);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=radiology-report-" + id + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
}