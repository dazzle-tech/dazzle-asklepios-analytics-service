package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.integration.ai.client.ConvoVoiceAgentClient;
import com.dazzle.asklepios.integration.ai.client.dto.ambientdraft.AmbientDraftOptionsDTO;
import com.dazzle.asklepios.integration.ai.client.dto.ambientdraft.AmbientRegenerateRequestDTO;
import com.dazzle.asklepios.integration.ai.service.mapper.AmbientDraftMapperService;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class AmbientDraftService {

    private static final Logger log = LoggerFactory.getLogger(AmbientDraftService.class);
    private static final String ENTITY_NAME = "ambientDraft";
    private static final Set<String> SECTIONS =
            Set.of("chief_complaint", "hpi", "review_of_systems", "physical_exam", "assessment_plan");

    private final ConvoVoiceAgentClient convoVoiceAgentClient;
    private final AmbientDraftMapperService ambientDraftMapperService;

    public AmbientDraftService(ConvoVoiceAgentClient convoVoiceAgentClient, AmbientDraftMapperService ambientDraftMapperService) {
        this.convoVoiceAgentClient = convoVoiceAgentClient;
        this.ambientDraftMapperService = ambientDraftMapperService;
    }

    public JsonNode draft(MultipartFile audio, AmbientDraftOptionsDTO options) {
        if (audio == null || audio.isEmpty()) {
            throw new BadRequestAlertException("Please record the visit first.", ENTITY_NAME, "audio.required");
        }

        Map<String, Object> form = new LinkedHashMap<>();
        form.put("audio", audio);
        putIfPresent(form, "note_language", options.noteLanguage());
        putIfPresent(form, "instruction_language", options.instructionLanguage());
        putIfPresent(form, "specialty", options.specialty());
        putIfPresent(form, "note_length", options.noteLength());
        putIfPresent(form, "note_form", options.noteForm());
        putIfPresent(form, "patient_context", options.patientContext());
        putIfPresent(form, "suggest_icd10", options.suggestIcd10());
        putIfPresent(form, "suggest_orders", options.suggestOrders());

        String clinicianId = SecurityUtils.getCurrentUserLogin().orElse(null);

        JsonNode draft;
        try {
            draft = convoVoiceAgentClient.draft(clinicianId, form);
        } catch (FeignException e) {
            log.error(
                    "Ambient draft failed. status={} error={}: {} response={}",
                    e.status(),
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e.contentUTF8(),
                    e
            );

            String message = e.status() == 400 || e.status() == 422
                    ? "The recording could not be processed. Please record the visit again."
                    : "Couldn't build the visit draft from your recording. Please try again later.";

            throw new BadRequestAlertException(message, ENTITY_NAME, "draft.failed");
        }

        return ambientDraftMapperService.map(draft);
    }

    public JsonNode regenerate(AmbientRegenerateRequestDTO request) {
        if (request.transcript() == null || !request.transcript().isArray() || request.transcript().isEmpty()) {
            throw new BadRequestAlertException("Record the visit first.", ENTITY_NAME, "transcript.required");
        }
        if (request.section() == null || !SECTIONS.contains(request.section())) {
            throw new BadRequestAlertException("Unknown section: " + request.section(), ENTITY_NAME, "section.invalid");
        }

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.set("transcript", request.transcript());
        body.put("section", request.section());
        body.put("instruction", request.instruction() == null ? "" : request.instruction().trim());
        putIfPresent(body, "note_language", request.noteLanguage());
        putIfPresent(body, "instruction_language", request.instructionLanguage());
        putIfPresent(body, "specialty", request.specialty());
        putIfPresent(body, "note_length", request.noteLength());
        putIfPresent(body, "note_form", request.noteForm());
        if (request.patientContext() != null && request.patientContext().isObject()) {
            body.set("patient_context", request.patientContext());
        }
        if (request.suggestIcd10() != null) {
            body.put("suggest_icd10", request.suggestIcd10());
        }
        if (request.suggestOrders() != null) {
            body.put("suggest_orders", request.suggestOrders());
        }

        String clinicianId = SecurityUtils.getCurrentUserLogin().orElse(null);

        JsonNode section;
        try {
            section = convoVoiceAgentClient.regenerate(clinicianId, body.toString());
        } catch (FeignException e) {
            log.error(
                    "Ambient section regenerate failed. section={} status={} error={}: {} response={}",
                    request.section(),
                    e.status(),
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e.contentUTF8(),
                    e
            );
            throw new BadRequestAlertException(
                    "Couldn't regenerate this section. Please try again later.", ENTITY_NAME, "regenerate.failed");
        }

        return ambientDraftMapperService.map(section);
    }

    private static void putIfPresent(ObjectNode body, String name, String value) {
        if (value != null && !value.isBlank()) {
            body.put(name, value.trim());
        }
    }

    private static void putIfPresent(Map<String, Object> form, String name, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (!text.isEmpty()) {
            form.put(name, text);
        }
    }
}
