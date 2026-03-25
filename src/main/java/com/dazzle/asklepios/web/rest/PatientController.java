package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.PatientService;
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

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }


    @GetMapping("/label/{id}")
    public ResponseEntity<PatientLabelDTO> getPatientLabel(@PathVariable Long id) {

        LOG.debug("[PatientLabel] request patientId={}", id);

        PatientLabelDTO patientLabelDTO = patientService.getPatientLabel(id);

        return ResponseEntity.ok(patientLabelDTO);
    }
}