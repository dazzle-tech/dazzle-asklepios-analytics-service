package com.dazzle.asklepios.service.dto;

import java.util.List;

public record DiagnosticOrderSampleLabelDTO(
        Long orderId,
        String patientName,
        String mrn,
        String facilityName,
        List<String> collectedTestShortNames
) {
}
