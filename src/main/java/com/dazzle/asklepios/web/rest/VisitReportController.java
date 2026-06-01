package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.VisitReportPdfRenderService;
import com.dazzle.asklepios.service.VisitReportService;
import com.dazzle.asklepios.service.dto.reports.VisitReportDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class VisitReportController {

    private static final Logger LOG = LoggerFactory.getLogger(VisitReportController.class);

    private final VisitReportService visitReportService;
    private final VisitReportPdfRenderService visitReportPdfRenderService;
    @GetMapping("/visit-report/{encounterId}")
    public ResponseEntity<VisitReportDTO> getVisitReport(@PathVariable Long encounterId) {

        LOG.debug("[VisitReport] request encounterId={}", encounterId);

        if (encounterId == null) {
            return ResponseEntity.badRequest().build();
        }

        VisitReportDTO report = visitReportService.getVisitReport(encounterId);

        if (report == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(report);
    }


    @GetMapping("/visit-report/{encounterId}/pdf")
    public ResponseEntity<byte[]> getVisitReportPdf(
            @PathVariable Long encounterId,
            @RequestParam(required = false) String timezone
    ) {
        LOG.debug("[REST][VISIT_REPORT_PDF] encounterId={}, timezone={}", encounterId, timezone);

        byte[] pdf = visitReportPdfRenderService.generateVisitReportPdf(encounterId, timezone);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("visit-report-" + encounterId + ".pdf")
                        .build()
        );

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}