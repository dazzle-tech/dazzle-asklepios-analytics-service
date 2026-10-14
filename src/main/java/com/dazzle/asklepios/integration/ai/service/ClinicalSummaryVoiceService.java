package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.integration.ai.client.ClinicalSummaryVoiceClient;
import com.dazzle.asklepios.integration.ai.client.dto.clinicalsummaryvoice.ClinicalSummaryVoiceResponseDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClinicalSummaryVoiceService {

    private static final Logger log =
            LoggerFactory.getLogger(ClinicalSummaryVoiceService.class);

    private final ClinicalSummaryVoiceClient clinicalSummaryVoiceClient;

    public ClinicalSummaryVoiceService(
            ClinicalSummaryVoiceClient clinicalSummaryVoiceClient
    ) {
        this.clinicalSummaryVoiceClient = clinicalSummaryVoiceClient;
    }

    public ClinicalSummaryVoiceResponseDTO summarize(MultipartFile audio) {
        try {
            ClinicalSummaryVoiceResponseDTO response =
                    clinicalSummaryVoiceClient.summarize(audio);

            if (response == null
                    || response.summary() == null
                    || response.summary().isBlank()) {
                throw new BadRequestAlertException(
                        "summary.generation.failed",
                        "clinicalSummaryVoice",
                        "Couldn't generate a clinical summary from your recording. Please try again."
                );
            }

            return response;

        } catch (FeignException e) {
            log.error(
                    "Clinical summary generation failed. Status: {}, response: {}",
                    e.status(),
                    e.contentUTF8()
            );

            String message = e.status() == 422
                    ? "No speech was detected in the recording. Please record again and speak clearly."
                    : "Couldn't generate a clinical summary from your recording. Please try again later.";

            throw new BadRequestAlertException(
                    "summary.generation.failed",
                    "clinicalSummaryVoice",
                    message
            );
        }
    }
}