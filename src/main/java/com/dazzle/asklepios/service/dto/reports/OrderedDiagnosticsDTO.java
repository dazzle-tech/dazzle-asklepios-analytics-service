package com.dazzle.asklepios.service.dto.reports;

public record OrderedDiagnosticsDTO(
        String orderNumber,
        String testName,
        String testType
)  {
}
