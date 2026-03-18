package com.dazzle.asklepios.web.rest.vm;

import java.math.BigDecimal;
import java.time.Instant;

public record DiagnosticOrderTestSampleLabelVM(
        Long orderTestId,
        String patientName,
        String facilityName,
        String mrn,
        String testName,
        Instant sampleDateTime,
        BigDecimal sampleQuantity,
        String sampleUnit
) {}
