package com.dazzle.asklepios.service.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DiagnosticOrderTestSampleLabelDTO(
        Long sampleId,
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
) {
}
