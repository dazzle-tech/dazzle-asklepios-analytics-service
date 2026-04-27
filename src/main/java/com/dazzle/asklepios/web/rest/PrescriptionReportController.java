package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.PrescriptionPdfRenderService;
import com.dazzle.asklepios.service.PrescriptionReportService;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionPrintDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class PrescriptionReportController {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionReportController.class);

    private final PrescriptionReportService prescriptionReportService;
    private final PrescriptionPdfRenderService prescriptionPdfRenderService;

    public PrescriptionReportController(PrescriptionReportService prescriptionReportService, PrescriptionPdfRenderService prescriptionPdfRenderService) {
        this.prescriptionReportService = prescriptionReportService;
        this.prescriptionPdfRenderService = prescriptionPdfRenderService;
    }

    @GetMapping("/prescriptions/report/{prescriptionId}")
    public ResponseEntity<PrescriptionPrintDTO> getPrescriptionPrint(@PathVariable Long prescriptionId) {
        LOG.debug("[PrescriptionPrintResource] GET_PRESCRIPTION_PRINT - start. prescriptionId={}", prescriptionId);

        PrescriptionPrintDTO dto = prescriptionReportService.getPrescriptionPrint(prescriptionId);

        LOG.debug("[PrescriptionPrintResource] GET_PRESCRIPTION_PRINT - completed. prescriptionId={}", prescriptionId);

        return ResponseEntity.ok(dto);
    }


    @GetMapping("/prescriptions/{prescriptionId}/pdf")
    public ResponseEntity<byte[]> getPrescriptionPdf(@PathVariable Long prescriptionId) {
        LOG.debug("[PrescriptionReportController] GET_PRESCRIPTION_PDF - start. prescriptionId={}", prescriptionId);

        byte[] pdf = prescriptionPdfRenderService.generatePrescriptionPdf(prescriptionId);

        LOG.debug("[PrescriptionReportController] GET_PRESCRIPTION_PDF - completed. prescriptionId={}, size={}", prescriptionId, pdf.length);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("prescription-" + prescriptionId + ".pdf")
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }

}