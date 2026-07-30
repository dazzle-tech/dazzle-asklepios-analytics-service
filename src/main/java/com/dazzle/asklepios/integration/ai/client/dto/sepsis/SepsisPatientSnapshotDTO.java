package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record SepsisPatientSnapshotDTO(
        String name,
        Integer age,
        String gender,
        BigDecimal weight,
        @JsonProperty("admission_reason")
        String admissionReason,
        List<String> comorbidities
) {
}
