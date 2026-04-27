package com.dazzle.asklepios.service.dto.reports;

import java.math.BigDecimal;

public record NurseSummaryVitalSignsDTO(
        Integer bloodPressureSystolic,
        Integer bloodPressureDiastolic,
        String measurementSite,
        Integer heartRate,
        BigDecimal temperature,
        Integer oxygenSaturation,
        Integer respiratoryRate,
        String notes,
        Integer painDegree
) {
}