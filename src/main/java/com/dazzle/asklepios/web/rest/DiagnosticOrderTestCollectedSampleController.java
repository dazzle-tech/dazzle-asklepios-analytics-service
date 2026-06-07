package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.DiagnosticOrderTestCollectedSampleService;
import com.dazzle.asklepios.service.DiagnosticOrderTestSampleLabelPdfRenderService;
import com.dazzle.asklepios.service.dto.DiagnosticOrderTestSampleLabelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class DiagnosticOrderTestCollectedSampleController {

    private static final Logger LOG =
            LoggerFactory.getLogger(DiagnosticOrderTestCollectedSampleController.class);

    private final DiagnosticOrderTestCollectedSampleService service;

    private final DiagnosticOrderTestSampleLabelPdfRenderService diagnosticOrderTestSampleLabelPdfRenderService;

    public DiagnosticOrderTestCollectedSampleController(
            DiagnosticOrderTestCollectedSampleService service,
            DiagnosticOrderTestSampleLabelPdfRenderService diagnosticOrderTestSampleLabelPdfRenderService
    ) {
        this.service = service;
        this.diagnosticOrderTestSampleLabelPdfRenderService =
                diagnosticOrderTestSampleLabelPdfRenderService;
    }

    @GetMapping("/diagnostic-order-test-collected-samples/sample-label/{orderTestId}")
    public ResponseEntity<DiagnosticOrderTestSampleLabelDTO> getSampleLabel(
            @PathVariable Long orderTestId
    ) {

        LOG.debug(
                "[SampleLabel] GET_SAMPLE_LABEL - request received. orderTestId={}",
                orderTestId
        );

        DiagnosticOrderTestSampleLabelDTO sampleLabelVM =
                service.getSampleLabel(orderTestId);

        LOG.debug(
                "[SampleLabel] GET_SAMPLE_LABEL - response ready. orderTestId={} patientName={} testName={}",
                orderTestId,
                sampleLabelVM.patientName(),
                sampleLabelVM.testName()
        );

        return ResponseEntity.ok(sampleLabelVM);
    }

    @GetMapping("/diagnostic-order-tests/{orderTestId}/sample-label/pdf")
    public ResponseEntity<byte[]> generateSampleLabelPdf(
            @PathVariable Long orderTestId,
            @RequestParam(defaultValue = "1") Integer copies
    ) {
        byte[] pdf =
                diagnosticOrderTestSampleLabelPdfRenderService.generateSampleLabelPdf(
                        orderTestId,
                        copies
                );

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "inline; filename=sample-label-" + orderTestId + ".pdf"
                )
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }

    @GetMapping("/diagnostic-order-tests/{orderTestId}/sample-labels/pdf")
    public ResponseEntity<byte[]> generateAllSampleLabelsPdf(
            @PathVariable Long orderTestId,
            @RequestParam(defaultValue = "1") Integer copies
    ) {
        byte[] pdf =
                diagnosticOrderTestSampleLabelPdfRenderService.generateAllSampleLabelsPdf(
                        orderTestId,
                        copies
                );

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "inline; filename=sample-labels-" + orderTestId + ".pdf"
                )
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
}