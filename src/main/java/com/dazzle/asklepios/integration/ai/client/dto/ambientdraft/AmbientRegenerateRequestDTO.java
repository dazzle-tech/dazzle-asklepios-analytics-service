package com.dazzle.asklepios.integration.ai.client.dto.ambientdraft;

import tools.jackson.databind.JsonNode;

public record AmbientRegenerateRequestDTO(
        JsonNode transcript,
        String section,
        String instruction,
        String noteLanguage,
        String instructionLanguage,
        String specialty,
        String noteLength,
        String noteForm,
        JsonNode patientContext,
        Boolean suggestIcd10,
        Boolean suggestOrders
) {
}
