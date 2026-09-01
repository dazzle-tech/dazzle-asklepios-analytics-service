package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.SettlementReportPdfRenderService;
import com.dazzle.asklepios.service.dto.SettlementReport.SettlementReportCriteriaDTO;
import com.dazzle.asklepios.service.dto.SettlementReport.SettlementReportRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class SettlementReportController {

    private final SettlementReportPdfRenderService pdfRenderService;

    @PostMapping(
            value = "/settlement/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> generateSettlementPdf(
            @RequestBody SettlementReportRequestDTO request,
            @RequestParam String timezone,
            @RequestParam(defaultValue = "en") String lang
    ) {

        byte[] pdf =
                pdfRenderService.generateSettlementReportPdf(
                        request,
                        timezone,
                        lang
                );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

