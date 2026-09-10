package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.AnalyticsReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsReportController {

    private final AnalyticsReportService analyticsReportService;

    @GetMapping(value = "reports/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePdf(@RequestParam String templateCode, @RequestParam Map<String, String> parameters) {

        // Remove templateCode because it is not a report variable
        parameters.remove("templateCode");

        byte[] pdf = analyticsReportService.generatePdf(
                templateCode,
                parameters
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + templateCode + ".pdf\""
                )
                .body(pdf);
    }
}
