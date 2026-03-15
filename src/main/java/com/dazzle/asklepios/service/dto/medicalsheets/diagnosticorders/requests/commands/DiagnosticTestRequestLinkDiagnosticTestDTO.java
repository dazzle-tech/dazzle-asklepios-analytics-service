package com.dazzle.asklepios.service.dto.medicalsheets.diagnosticorders.requests.commands;

import jakarta.validation.constraints.NotNull;
public record DiagnosticTestRequestLinkDiagnosticTestDTO(
        @NotNull Long diagnosticTestId
) {}
