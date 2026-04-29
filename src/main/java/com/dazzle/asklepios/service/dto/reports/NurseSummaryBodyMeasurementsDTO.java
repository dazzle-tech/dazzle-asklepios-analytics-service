package com.dazzle.asklepios.service.dto.reports;

import java.math.BigDecimal;

public record NurseSummaryBodyMeasurementsDTO(
        BigDecimal weight,
        BigDecimal height,
        BigDecimal headCircumference
) {
}