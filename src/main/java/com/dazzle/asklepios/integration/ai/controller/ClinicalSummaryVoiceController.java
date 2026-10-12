package com.dazzle.asklepios.integration.ai.controller;


import com.dazzle.asklepios.integration.ai.client.dto.clinicalsummaryvoice.ClinicalSummaryVoiceResponseDTO;
import com.dazzle.asklepios.integration.ai.service.ClinicalSummaryVoiceService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analytics")
public class ClinicalSummaryVoiceController {

    private final ClinicalSummaryVoiceService clinicalSummaryVoiceService;

    public ClinicalSummaryVoiceController(
            ClinicalSummaryVoiceService clinicalSummaryVoiceService
    ) {
        this.clinicalSummaryVoiceService = clinicalSummaryVoiceService;
    }

    @PostMapping(
            value = "/clinical-summary-voice/summarize",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ClinicalSummaryVoiceResponseDTO summarize(
            @RequestPart("audio") MultipartFile audio
    ) {
        return clinicalSummaryVoiceService.summarize(audio);
    }
}