package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.integration.ai.client.ClinicalSummaryVoiceClient;
import com.dazzle.asklepios.integration.ai.client.dto.clinicalsummaryvoice.ClinicalSummaryVoiceResponseDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Service
public class ClinicalSummaryVoiceService {

    private static final Logger log =
            LoggerFactory.getLogger(ClinicalSummaryVoiceService.class);

    private static final String ENTITY_NAME = "clinicalSummaryVoice";

    private final ClinicalSummaryVoiceClient clinicalSummaryVoiceClient;
    private final ObjectMapper objectMapper;

    public ClinicalSummaryVoiceService(
            ClinicalSummaryVoiceClient clinicalSummaryVoiceClient,
            ObjectMapper objectMapper
    ) {
        this.clinicalSummaryVoiceClient = clinicalSummaryVoiceClient;
        this.objectMapper = objectMapper;
    }

    public ClinicalSummaryVoiceResponseDTO summarize(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw error(
                    "Audio file is missing or empty",
                    "The recording is empty. Please record again."
            );
        }

        ClinicalSummaryVoiceResponseDTO response;
        try {
            response = clinicalSummaryVoiceClient.summarize(audio);
        } catch (RetryableException e) {
            // Request never got a response: connection problem or timeout
            throw connectionError(e);
        } catch (FeignException e) {
            // The voice service answered with an error status
            throw responseError(e);
        }

        if (response == null
                || response.summary() == null
                || response.summary().isBlank()) {
            log.error("Clinical summary voice service returned an empty summary");
            throw error(
                    "Voice service returned an empty summary",
                    "The recording was processed but no summary was produced. Please record again with more detail."
            );
        }

        return response;
    }

    /** No HTTP response from the voice service (host not found, connection refused, timeout). */
    private BadRequestAlertException connectionError(RetryableException e) {
        Throwable cause = rootCause(e);
        log.error("Clinical summary voice service is unreachable: {} ({})",
                e.getMessage(), cause.getClass().getSimpleName(), e);

        if (cause instanceof SocketTimeoutException) {
            return error(
                    "Voice service timed out: " + cause.getMessage(),
                    "The clinical summary took too long to generate. Please try a shorter recording or try again later."
            );
        }
        if (cause instanceof UnknownHostException || cause instanceof ConnectException) {
            return error(
                    "Voice service is unreachable: " + cause.getClass().getSimpleName() + " " + cause.getMessage(),
                    "The clinical summary service is not reachable right now. Please contact support if this continues."
            );
        }
        return error(
                "Voice service connection failed: " + cause.getClass().getSimpleName() + " " + cause.getMessage(),
                "Couldn't connect to the clinical summary service. Please try again later."
        );
    }

    /** The voice service responded with an error status; map it to a specific message. */
    private BadRequestAlertException responseError(FeignException e) {
        int status = e.status();
        String detail = extractDetail(e.contentUTF8());
        log.error("Clinical summary voice service failed. Status: {}, detail: {}, response: {}",
                status, detail, e.contentUTF8());

        String technical = "Voice service returned " + status + (detail.isEmpty() ? "" : ": " + detail);

        return switch (status) {
            case 400 -> error(technical,
                    "The recording was not received correctly. Please record again.");
            case 404 -> error(technical,
                    "The clinical summary service is not configured correctly. Please contact support.");
            case 413 -> error(technical,
                    "The recording is too long. Please record a shorter message.");
            case 422 -> error(technical,
                    "No speech was detected in the recording. Please record again and speak clearly.");
            case 502 -> detail.startsWith("Transcription")
                    ? error(technical,
                    "Couldn't convert the recording to text. Please check the audio quality and try again.")
                    : error(technical,
                    "Couldn't create a summary from the recording. Please try again.");
            case 503 -> error(technical,
                    "The clinical summary service is not configured (AI provider unavailable). Please contact support.");
            case 504 -> error(technical,
                    "The clinical summary took too long to generate. Please try a shorter recording or try again later.");
            default -> error(technical,
                    "Couldn't generate a clinical summary from your recording (error " + status + "). Please try again later.");
        };
    }

    /** The voice service (FastAPI) returns errors as {"detail": "..."}; fall back to the raw body. */
    private String extractDetail(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try {
            JsonNode detail = objectMapper.readTree(body).get("detail");
            if (detail != null) {
                return detail.isTextual() ? detail.asText() : detail.toString();
            }
        } catch (Exception ignored) {
            // not JSON — use the raw body
        }
        return body.length() > 300 ? body.substring(0, 300) : body;
    }

    private static Throwable rootCause(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * title = technical reason (for logs / network tab),
     * message = user-facing text (the frontend shows it after stripping "error.").
     */
    private static BadRequestAlertException error(String technicalReason, String userMessage) {
        return new BadRequestAlertException(technicalReason, ENTITY_NAME, userMessage);
    }
}