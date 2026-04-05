package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.service.PatientService;
import com.dazzle.asklepios.service.dto.patient.PatientCreateDTO;
import com.dazzle.asklepios.service.dto.patient.PatientDuplicationLookupDTO;
import com.dazzle.asklepios.service.dto.patient.PatientInformationReportDTO;
import com.dazzle.asklepios.service.dto.patient.PatientUpdateDTO;
import com.dazzle.asklepios.service.dto.patient.PatientWristbandDTO;
import com.dazzle.asklepios.service.dto.patient.UnknownPatientCreateDTO;
import com.dazzle.asklepios.service.dto.patientLabel.PatientLabelDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.patient.PatientBasicInformationResponseVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class PatientController {

    private static final Logger LOG =
            LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }


    @GetMapping("/{patientId}/information-report")
    public ResponseEntity<PatientInformationReportDTO> getPatientInformationReport(
            @PathVariable Long patientId
    ) {

        LOG.debug("[PatientReport] GET_PATIENT_INFORMATION_REPORT request patientId={}", patientId);

        PatientInformationReportDTO result =
                patientService.getPatientInformationReport(patientId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{patientId}/wristband")
    public ResponseEntity<PatientWristbandDTO> getPatientWristband(
            @PathVariable Long patientId
    ) {
        return ResponseEntity.ok(patientService.getPatientWristband(patientId));
    }

    @GetMapping("/label/{id}")
    public ResponseEntity<PatientLabelDTO> getPatientLabel(@PathVariable Long id) {

        LOG.debug("[PatientLabel] request patientId={}", id);

        PatientLabelDTO patientLabelDTO = patientService.getPatientLabel(id);

        return ResponseEntity.ok(patientLabelDTO);
    }
}