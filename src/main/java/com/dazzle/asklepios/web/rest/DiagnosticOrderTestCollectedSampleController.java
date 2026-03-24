package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.DiagnosticOrderTestCollectedSampleService;
import com.dazzle.asklepios.service.dto.DiagnosticOrderTestSampleLabelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class DiagnosticOrderTestCollectedSampleController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderTestCollectedSampleController.class);

    private final DiagnosticOrderTestCollectedSampleService service;


    public DiagnosticOrderTestCollectedSampleController(
            DiagnosticOrderTestCollectedSampleService service

    ) {
        this.service = service;

    }


    @GetMapping("/diagnostic-order-test-collected-samples/sample-label/{orderTestId}")
    public ResponseEntity<DiagnosticOrderTestSampleLabelDTO> getSampleLabel(@PathVariable Long orderTestId) {

        LOG.debug("[SampleLabel] GET_SAMPLE_LABEL - request received. orderTestId={}", orderTestId);

        DiagnosticOrderTestSampleLabelDTO sampleLabelVM = service.getSampleLabel(orderTestId);

        LOG.debug(
                "[SampleLabel] GET_SAMPLE_LABEL - response ready. orderTestId={} patientName={} testName={}",
                orderTestId,
                sampleLabelVM.patientName(),
                sampleLabelVM.testName()
        );

        return ResponseEntity.ok(sampleLabelVM);
    }
}
