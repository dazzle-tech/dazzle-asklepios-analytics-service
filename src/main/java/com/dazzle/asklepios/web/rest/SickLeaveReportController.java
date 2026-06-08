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
import com.dazzle.asklepios.service.dto.sick_leave_report.SickLeaveReportRequestDTO;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class SickLeaveReportController {

    private static final Logger LOG = LoggerFactory.getLogger(SickLeaveReportController.class);

    private final SickLeaveReportService sickLeaveReportService;
    private final SickLeaveReportPdfRenderService sickLeaveReportPdfRenderService;



    @PostMapping("/sick-leave-report/{encounterId}")
    public ResponseEntity<SickLeaveReportDTO> postSickLeaveReport(
            @PathVariable Long encounterId,
            @RequestBody SickLeaveReportRequestDTO request
    ) {
        LOG.debug("[SickLeaveReport][POST] request encounterId={} fromDate={} toDate={} notes={}", encounterId, request.fromDate(), request.toDate(), request.notes());

        if (encounterId == null) {
            return ResponseEntity.badRequest().build();
        }

        SickLeaveReportDTO report = sickLeaveReportService.getSickLeaveReport(encounterId, request.fromDate(), request.toDate(), request.notes());

        if (report == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(report);
    }

    @PostMapping("/sick-leave-report/{encounterId}/pdf")
    public ResponseEntity<byte[]> postSickLeaveReportPdf(
            @PathVariable Long encounterId,
            @RequestParam(required = false) String timezone,
            @RequestParam  (defaultValue = "en") String lang,
            @RequestBody SickLeaveReportRequestDTO request
    ) {
        LOG.debug(
                "[REST][SICK_LEAVE_PDF][POST] encounterId={} timezone={} fromDate={} toDate={} notes={}",
                encounterId,
                timezone,
                request.fromDate(),
                request.toDate(),
                request.notes()
        );

        SickLeaveReportDTO dto = sickLeaveReportService.getSickLeaveReport(
                encounterId,
                request.fromDate(),
                request.toDate(),
                request.notes()
        );

        byte[] pdf = sickLeaveReportPdfRenderService.generateSickLeaveReportPdf(dto, timezone ,lang);

        LOG.debug(
                "[REST][SICK_LEAVE_PDF][POST] completed. encounterId={} timezone={} notes={} size={}",
                encounterId,
                timezone,
                request.notes(),
                pdf.length
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("sick-leave-report-E0" + encounterId + ".pdf")
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }
}
