package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.service.AmbientDraftService;
import com.dazzle.asklepios.integration.ai.client.dto.ambientdraft.AmbientDraftOptionsDTO;
import tools.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analytics")
public class AmbientDraftController {

    private static final Logger LOG = LoggerFactory.getLogger(AmbientDraftController.class);

    private final AmbientDraftService ambientDraftService;

    public AmbientDraftController(AmbientDraftService ambientDraftService) {
        this.ambientDraftService = ambientDraftService;
    }

    @PostMapping(
            value = "/ambient-draft",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public JsonNode draft(
            @RequestPart("audio") MultipartFile audio,
            @RequestParam(value = "noteLanguage", required = false) String noteLanguage,
            @RequestParam(value = "instructionLanguage", required = false) String instructionLanguage,
            @RequestParam(value = "specialty", required = false) String specialty,
            @RequestParam(value = "noteLength", required = false) String noteLength,
            @RequestParam(value = "noteForm", required = false) String noteForm,
            @RequestParam(value = "patientContext", required = false) String patientContext,
            @RequestParam(value = "suggestIcd10", required = false) Boolean suggestIcd10,
            @RequestParam(value = "suggestOrders", required = false) Boolean suggestOrders
    ) {
        LOG.debug("REST ambient draft size={} suggestIcd10={} suggestOrders={}", audio.getSize(), suggestIcd10, suggestOrders);

        return ambientDraftService.draft(
                audio,
                new AmbientDraftOptionsDTO(
                        noteLanguage,
                        instructionLanguage,
                        specialty,
                        noteLength,
                        noteForm,
                        patientContext,
                        suggestIcd10,
                        suggestOrders
                )
        );
    }
}
