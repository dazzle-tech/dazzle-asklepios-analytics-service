package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.AnalyticsReportService;
import com.dazzle.asklepios.service.VisitReportPdfRenderService;
import com.dazzle.asklepios.service.VisitReportService;
import com.dazzle.asklepios.service.dto.PatientEncounterReportDTO;
import com.dazzle.asklepios.service.dto.reports.VisitReportDTO;
import com.dazzle.asklepios.service.dto.reports.dailyPatientVisit.DailyPatientVisitDTO;
import com.dazzle.asklepios.web.rest.vm.report.totalDailyFootfall.TotalDailyFootfallResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class VisitReportController {

    private static final Logger LOG = LoggerFactory.getLogger(VisitReportController.class);

    private final VisitReportService visitReportService;
    private final VisitReportPdfRenderService visitReportPdfRenderService;
    private final AnalyticsReportService analyticsReportService;

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
            @RequestParam(required = false) String timezone,
            @RequestParam(defaultValue = "en") String lang
    ) {
        LOG.debug(
                "[REST][VISIT_REPORT_PDF] encounterId={}, timezone={}, lang={}",
                encounterId,
                timezone,
                lang
        );

        byte[] pdf = visitReportPdfRenderService.generateVisitReportPdf(
                encounterId,
                timezone,
                lang
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("visit-report-" + encounterId + ".pdf")
                        .build()
        );

        return ResponseEntity.ok().headers(headers).body(pdf);
    }


    @GetMapping("/patient-encounters")
    public ResponseEntity<List<PatientEncounterReportDTO>>
    getPatientEncounters() {

        return ResponseEntity.ok(
                visitReportService.getAllEncounter()
        );
    }

    @GetMapping("/daily-visits")
    public ResponseEntity<List<DailyPatientVisitDTO>> getDailyPatientVisits(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
      LOG.debug("[VisitReport] request daily patient visits for date {}", date);
        return ResponseEntity.ok(
                visitReportService.getDailyPatientVisits(date)
        );
    }


}