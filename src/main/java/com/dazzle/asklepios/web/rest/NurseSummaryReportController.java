package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.NurseSummaryPdfRenderService;
import com.dazzle.asklepios.service.NurseSummaryReportService;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class NurseSummaryReportController {

    private static final Logger LOG = LoggerFactory.getLogger(NurseSummaryReportController.class);

    private final NurseSummaryReportService nurseSummaryReportService;
    private final NurseSummaryPdfRenderService nurseSummaryPdfRenderService;

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
    public ResponseEntity<byte[]> getNurseSummaryReportPdf(
            @PathVariable Long encounterId,
            @RequestParam (defaultValue = "en") String lang
    ) {
        LOG.debug("[REST][NURSE_SUMMARY_PDF] encounterId={}", encounterId);

        byte[] pdf = nurseSummaryPdfRenderService.generateNurseSummaryPdf(encounterId,lang);

        LOG.debug("[REST][NURSE_SUMMARY_PDF] completed. encounterId={}, size={}", encounterId, pdf.length);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("nurse-summary-" + encounterId + ".pdf")
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }
}