package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.ProcedureReportPdfRenderService;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class ProcedureReportController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureReportController.class);

    private final ProcedureReportPdfRenderService procedureReportPdfRenderService;

    /**
     * The report data as JSON, for previews or other clients.
     */
    @GetMapping("/procedures/{procedureRequestId}/report")
    public ResponseEntity<ProcedureReportDTO> getProcedureReport(
            @PathVariable Long procedureRequestId,
            @RequestParam(required = false) String timezone,
            @RequestParam(defaultValue = "en") String lang
    ) {
        LOG.debug("[REST][PROCEDURE_REPORT][GET] procedureRequestId={} timezone={} lang={}", procedureRequestId, timezone, lang);
        return ResponseEntity.ok(procedureReportPdfRenderService.getReport(procedureRequestId, timezone, lang));
    }

    /**
     * The procedure report PDF for one patient procedure request ({@code patient_procedure.id}).
     */
    @GetMapping("/procedures/{procedureRequestId}/report/pdf")
    public ResponseEntity<byte[]> generateProcedureReportPdf(
            @PathVariable Long procedureRequestId,
            @RequestParam(required = false) String timezone,
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(required = false) String verifyBaseUrl
    ) {
        LOG.debug("[REST][PROCEDURE_REPORT_PDF][GET] procedureRequestId={} timezone={} lang={} verifyBaseUrl={}",
                procedureRequestId, timezone, lang, verifyBaseUrl);

        byte[] pdf = procedureReportPdfRenderService.generateProcedureReportPdf(procedureRequestId, timezone, lang, verifyBaseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("procedure-report-" + procedureRequestId + ".pdf")
                .build());

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
