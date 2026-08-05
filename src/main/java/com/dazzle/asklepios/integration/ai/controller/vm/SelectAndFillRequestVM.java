package com.dazzle.asklepios.integration.ai.controller.vm;

public record SelectAndFillRequestVM(
        Long reportId,
        String outputLanguage
) {
}