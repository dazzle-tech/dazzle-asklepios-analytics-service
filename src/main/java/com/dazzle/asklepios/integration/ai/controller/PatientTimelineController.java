package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.timeline.TimelineResponseDTO;
import com.dazzle.asklepios.integration.ai.service.PatientTimelineIntegrationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class PatientTimelineController {

    private static final Logger LOG = LoggerFactory.getLogger(PatientTimelineController.class);

    private final PatientTimelineIntegrationService patientTimelineIntegrationService;

    @GetMapping("/patients/{patientId}/timeline")
    public ResponseEntity<TimelineResponseDTO> getPatientTimeline(@PathVariable Long patientId) {
        LOG.debug("[REST][PATIENT_TIMELINE][GET] patientId={}", patientId);
        return ResponseEntity.ok(patientTimelineIntegrationService.generateTimeline(patientId));
    }
}
