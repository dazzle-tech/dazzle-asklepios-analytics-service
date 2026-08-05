package com.dazzle.asklepios.integration.ai.controller;


import com.dazzle.asklepios.integration.ai.client.dto.radiology.SelectAndFillResponseDTO;
import com.dazzle.asklepios.integration.ai.controller.vm.SelectAndFillRequestVM;
import com.dazzle.asklepios.integration.ai.service.RadiologyAutofillAiService;
import com.dazzle.asklepios.web.rest.DiagnosticOrderTestReportController;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class RadiologyAutofillController {
    private static final Logger LOG = LoggerFactory.getLogger(RadiologyAutofillController.class);

    private final RadiologyAutofillAiService service;
    @PostMapping("/radiology/select-and-fill")
    public ResponseEntity<SelectAndFillResponseDTO> selectAndFill(
            @RequestBody SelectAndFillRequestVM request
    ) {

        LOG.debug("REST request to select and fill radiology report: {}", request);

        SelectAndFillResponseDTO response =
                service.selectAndFill(request);

        return ResponseEntity.ok(response);
    }
}