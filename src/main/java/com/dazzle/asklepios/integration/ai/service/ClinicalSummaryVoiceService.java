package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.integration.ai.client.ClinicalSummaryVoiceClient;
import com.dazzle.asklepios.integration.ai.client.dto.clinicalsummaryvoice.ClinicalSummaryVoiceResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClinicalSummaryVoiceService {

    private final ClinicalSummaryVoiceClient clinicalSummaryVoiceClient;

    public ClinicalSummaryVoiceService(
            ClinicalSummaryVoiceClient clinicalSummaryVoiceClient
    ) {
        this.clinicalSummaryVoiceClient = clinicalSummaryVoiceClient;
    }

    public ClinicalSummaryVoiceResponseDTO summarize(MultipartFile audio) {
        return clinicalSummaryVoiceClient.summarize(audio);
    }
}