package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.PatientVaccinationReportPdfRenderService;
import com.dazzle.asklepios.service.PatientVaccinationReportService;
import com.dazzle.asklepios.service.dto.reports.vaccination.PatientVaccinationReportDTO;
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
public class PatientVaccinationReportController {

    private static final Logger LOG = LoggerFactory.getLogger(PatientVaccinationReportController.class);

    private final PatientVaccinationReportService patientVaccinationReportService;
    private final PatientVaccinationReportPdfRenderService patientVaccinationReportPdfRenderService;

    @GetMapping("/{patientId}/vaccination-report")
    public ResponseEntity<PatientVaccinationReportDTO> getPatientVaccinationReport(
            @PathVariable Long patientId
    ) {
        LOG.debug("[REST][PATIENT_VACCINATION_REPORT][GET] patientId={}", patientId);

        return ResponseEntity.ok(patientVaccinationReportService.getPatientVaccinationReport(patientId));
    }

    @GetMapping("/{patientId}/vaccination-report/pdf")
    public ResponseEntity<byte[]> generatePatientVaccinationReportPdf(
            @PathVariable Long patientId,
            @RequestParam(required = false) String timezone,
            @RequestParam(defaultValue = "en") String lang
    ) {
        LOG.debug(
                "[REST][PATIENT_VACCINATION_REPORT_PDF][GET] patientId={} timezone={} lang={}",
                patientId,
                timezone,
                lang
        );

        byte[] pdf = patientVaccinationReportPdfRenderService.generatePatientVaccinationReportPdf(
                patientId,
                timezone,
                lang
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("vaccination-report-" + patientId + ".pdf")
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }
}
