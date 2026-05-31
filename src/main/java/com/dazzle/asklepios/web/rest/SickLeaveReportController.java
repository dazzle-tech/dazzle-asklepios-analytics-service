package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.SickLeaveReportPdfRenderService;
import com.dazzle.asklepios.service.SickLeaveReportService;
import com.dazzle.asklepios.service.dto.reports.SickLeaveReportDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class SickLeaveReportController {

    private static final Logger LOG = LoggerFactory.getLogger(SickLeaveReportController.class);

    private final SickLeaveReportService sickLeaveReportService;
    private final SickLeaveReportPdfRenderService sickLeaveReportPdfRenderService;

    @GetMapping("/sick-leave-report/{encounterId}")
    public ResponseEntity<SickLeaveReportDTO> getSickLeaveReport(
            @PathVariable Long encounterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        LOG.debug("[SickLeaveReport] request encounterId={} fromDate={} toDate={}", encounterId, fromDate, toDate);

        if (encounterId == null) {
            return ResponseEntity.badRequest().build();
        }

        SickLeaveReportDTO report = sickLeaveReportService.getSickLeaveReport(encounterId, fromDate, toDate);

        if (report == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(report);
    }

    @GetMapping("/sick-leave-report/{encounterId}/pdf")
    public ResponseEntity<byte[]> getSickLeaveReportPdf(
            @PathVariable Long encounterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        LOG.debug("[REST][SICK_LEAVE_PDF] encounterId={} fromDate={} toDate={}", encounterId, fromDate, toDate);

        byte[] pdf = sickLeaveReportPdfRenderService.generateSickLeaveReportPdf(encounterId, fromDate, toDate);

        LOG.debug("[REST][SICK_LEAVE_PDF] completed. encounterId={}, size={}", encounterId, pdf.length);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("sick-leave-report-" + encounterId + ".pdf")
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }
}
