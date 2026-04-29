package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.PatientLabelPdfRenderService;
import com.dazzle.asklepios.service.PatientService;
import com.dazzle.asklepios.service.PatientWristbandPdfRenderService;
import com.dazzle.asklepios.service.dto.patient.PatientWristbandDTO;
import com.dazzle.asklepios.service.dto.patientLabel.PatientLabelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class PatientController {

    private static final Logger LOG =
            LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;
   private final PatientWristbandPdfRenderService patientWristbandPdfRenderService;
   private final PatientLabelPdfRenderService  patientLabelPdfRenderService;
    public PatientController(PatientService patientService, PatientWristbandPdfRenderService patientWristbandPdfRenderService, PatientLabelPdfRenderService patientLabelPdfRenderService) {
        this.patientService = patientService;
        this.patientWristbandPdfRenderService = patientWristbandPdfRenderService;
        this.patientLabelPdfRenderService = patientLabelPdfRenderService;
    }


    @GetMapping("/label/{id}")
    public ResponseEntity<PatientLabelDTO> getPatientLabel(@PathVariable Long id) {

        LOG.debug("[PatientLabel] request patientId={}", id);

        PatientLabelDTO patientLabelDTO = patientService.getPatientLabel(id);

        return ResponseEntity.ok(patientLabelDTO);
    }
    @GetMapping("/{patientId}/wristband")
    public ResponseEntity<PatientWristbandDTO> getPatientWristband(
            @PathVariable Long patientId
    ) {
        return ResponseEntity.ok(patientService.getPatientWristband(patientId));
    }
    @GetMapping("/{patientId}/wristband/pdf")
    public ResponseEntity<byte[]> generateWristbandPdf(@PathVariable Long patientId) {

        byte[] pdf = patientWristbandPdfRenderService.generateWristbandPdf(patientId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=wristband-" + patientId + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
    @GetMapping("{id}/label/pdf")
    public ResponseEntity<byte[]> generatePatientLabelPdf(@PathVariable Long id) {

        byte[] pdf = patientLabelPdfRenderService.generatePatientLabelPdf(id);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=patient-label-" + id + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
}
