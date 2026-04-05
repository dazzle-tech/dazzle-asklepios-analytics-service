package com.dazzle.asklepios.service.dto.reports;

public record OrderedDiagnosticsDTO(
        Long orderNumber,
        String testName,
        String testType
)  {
}
