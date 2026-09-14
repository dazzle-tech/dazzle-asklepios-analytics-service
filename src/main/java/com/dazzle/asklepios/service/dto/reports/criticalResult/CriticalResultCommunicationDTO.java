package com.dazzle.asklepios.service.dto.reports.criticalResult;

import com.dazzle.asklepios.domain.enumeration.TestType;

import java.time.Instant;

public record CriticalResultCommunicationDTO(
        Long resultId,
        TestType testType,
        Instant criticalAt,
        Instant communicatedAt
) {
}
