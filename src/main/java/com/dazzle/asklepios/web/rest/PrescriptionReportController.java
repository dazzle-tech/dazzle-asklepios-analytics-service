package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.PrescriptionReportService;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionPrintDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public PrescriptionReportController(PrescriptionReportService prescriptionReportService) {
        this.prescriptionReportService = prescriptionReportService;
    }

    @GetMapping("/prescriptions/report/{prescriptionId}")
    public ResponseEntity<PrescriptionPrintDTO> getPrescriptionPrint(@PathVariable Long prescriptionId) {
        LOG.debug("[PrescriptionPrintResource] GET_PRESCRIPTION_PRINT - start. prescriptionId={}", prescriptionId);

        PrescriptionPrintDTO dto = prescriptionReportService.getPrescriptionPrint(prescriptionId);

        LOG.debug("[PrescriptionPrintResource] GET_PRESCRIPTION_PRINT - completed. prescriptionId={}", prescriptionId);

        return ResponseEntity.ok(dto);
    }
}