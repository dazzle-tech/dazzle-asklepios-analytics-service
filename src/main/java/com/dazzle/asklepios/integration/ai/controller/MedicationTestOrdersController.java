package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.MedicationLookupRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.TestOrdersLookupRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.medvalidation.ValidationResponseDTO;
import com.dazzle.asklepios.integration.ai.service.MedicationTestOrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class MedicationTestOrdersController {

    private final MedicationTestOrdersService medicationTestOrdersService;

    @PostMapping("/validate/medication")
    public ResponseEntity<ValidationResponseDTO> validateMedication(
            @RequestBody MedicationLookupRequestDTO request
    ) {
        return ResponseEntity.ok(medicationTestOrdersService.validateMedication(request));
    }

    @PostMapping("/validate/tests")
    public ResponseEntity<ValidationResponseDTO> validateTests(
            @RequestBody TestOrdersLookupRequestDTO request
    ) {
        return ResponseEntity.ok(medicationTestOrdersService.validateTests(request));
    }
}
