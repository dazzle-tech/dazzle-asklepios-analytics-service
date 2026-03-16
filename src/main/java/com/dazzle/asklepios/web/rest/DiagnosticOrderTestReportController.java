package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticOrderTestReport;
import com.dazzle.asklepios.service.DiagnosticOrderTestReportService;
import com.dazzle.asklepios.web.rest.vm.RadiologyReportVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing radiology reports ({@link DiagnosticOrderTestReport}).
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Expose CRUD endpoints for reports.</li>
 *   <li>Expose controlled endpoints for review/reject/approve and image workflow.</li>
 *   <li>Expose filtering endpoint for reports.</li>
 * </ul>
 *
 * <p>Note: This controller does not access repositories directly; all persistence and
 * validation logic is delegated to {@link DiagnosticOrderTestReportService}.</p>
 */
@RestController
@RequestMapping("/api/analytics")
public class DiagnosticOrderTestReportController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderTestReportController.class);

    private final DiagnosticOrderTestReportService reportService;

    public DiagnosticOrderTestReportController(DiagnosticOrderTestReportService reportService) {
        this.reportService = reportService;

    }

    @GetMapping("/radiology-reports/{reportId}")
    public ResponseEntity<RadiologyReportVM> getRadiologyReport(@PathVariable Long reportId) {

        RadiologyReportVM report = reportService.getRadiologyReport(reportId);

        return ResponseEntity.ok(report);
    }
}