package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.DentalTreatmentPlanPdfRenderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/analytics/dental-treatment-plans")
public class DentalTreatmentPlanReportController {

    private final DentalTreatmentPlanPdfRenderService pdfRenderService;

    public DentalTreatmentPlanReportController(DentalTreatmentPlanPdfRenderService pdfRenderService) {
        this.pdfRenderService = pdfRenderService;
    }

    @GetMapping("/{planId}/pdf")
    public ResponseEntity<byte[]> pdf(
            @PathVariable Long planId,
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(required = false) String attachmentIds
    ) {
        byte[] pdf = pdfRenderService.generate(planId, lang, parseIds(attachmentIds));
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=dental-treatment-plan-" + planId + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }

    private List<Long> parseIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (String part : raw.split(",")) {
            if (!part.isBlank()) {
                ids.add(Long.valueOf(part.trim()));
            }
        }
        return ids;
    }
}
