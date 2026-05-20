package com.dazzle.asklepios.service.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record DiagnosticOrderTestSampleLabelDTO(
        Long orderTestId,
        String patientName,
        String facilityName,
        String mrn,
        String testName,
        Instant sampleDateTime,
        BigDecimal sampleQuantity,
        String sampleUnit,
        Instant expiryDate,
        String sourceOfSample
) {}
