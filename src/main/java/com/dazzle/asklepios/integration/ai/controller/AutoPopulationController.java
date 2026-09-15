package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.AutoPopulateRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.autopopulation.AutoPopulationResponseDTO;
import com.dazzle.asklepios.integration.ai.service.AutoPopulationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/auto-Population")
@RequiredArgsConstructor
public class AutoPopulationController {

    private static final Logger LOG =
            LoggerFactory.getLogger(AutoPopulationController.class);

    private final AutoPopulationService autoPopulationService;

    @PostMapping("/auto-populate")
    public ResponseEntity<AutoPopulationResponseDTO> autoPopulate(@RequestBody AutoPopulateRequestDTO request) {
        LOG.debug(
                "Request to auto populate user text={} patient id={}",
                request.userText(),
                request.patientId()
        );
        return ResponseEntity.ok(autoPopulationService.autoPopulate(request));
    }
}
