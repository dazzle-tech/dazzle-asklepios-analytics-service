package com.dazzle.asklepios.service.dto.reports.criticalResult;


import java.time.Instant;

public record CriticalResultCommunicationDTO(
        Long resultId,
        String testType,
        Instant criticalAt,
        Instant communicatedAt
) {
}
