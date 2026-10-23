package com.dazzle.asklepios.integration.ai.client.dto.ambientdraft;

public record AmbientDraftOptionsDTO(
        String noteLanguage,
        String instructionLanguage,
        String specialty,
        String noteLength,
        String noteForm,
        String patientContext,
        Boolean suggestIcd10,
        Boolean suggestOrders
) {
}
