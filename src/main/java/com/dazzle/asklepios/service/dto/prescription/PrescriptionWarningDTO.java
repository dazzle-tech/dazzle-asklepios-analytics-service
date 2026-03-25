package com.dazzle.asklepios.service.dto.prescription;

public record PrescriptionWarningDTO(
        String type,
        String warning,
        String severity
) {}