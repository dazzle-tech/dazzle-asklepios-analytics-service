package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.NurseSummaryReportService;
import com.dazzle.asklepios.service.NurseSummaryPdfRenderService;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class NurseSummaryReportController {

    private static final Logger LOG = LoggerFactory.getLogger(NurseSummaryReportController.class);

    private final NurseSummaryReportService nurseSummaryReportService;
    private final NurseSummaryPdfRenderService nurseSummaryPdfRenderService; // 👈 جديد

    @GetMapping("/nurse-summary/{encounterId}")
    public ResponseEntity<NurseSummaryReportDTO> getNurseSummaryReport(
            @PathVariable Long encounterId
    ) {
        LOG.debug("[REST][NURSE_SUMMARY] encounterId={}", encounterId);
        return ResponseEntity.ok(
                nurseSummaryReportService.getNurseSummaryReport(encounterId)
        );
    }

    @GetMapping("/nurse-summary/{encounterId}/pdf")
    public ResponseEntity<byte[]> getNurseSummaryPdf(
            @PathVariable Long encounterId
    ) {
        LOG.debug("[REST][NURSE_SUMMARY_PDF] encounterId={}", encounterId);

        byte[] pdf = nurseSummaryPdfRenderService.generateNurseSummaryPdf(encounterId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=nurse-summary-" + encounterId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}