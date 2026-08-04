package com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning;

public record ClinicalNoteDTO(
        String type,
        String timestamp,
        String text
) {}
