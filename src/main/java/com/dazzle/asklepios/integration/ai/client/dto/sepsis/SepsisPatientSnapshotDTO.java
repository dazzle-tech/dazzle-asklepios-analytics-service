package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record SepsisPatientSnapshotDTO(
        String name,
        Integer age,
        String gender,
        BigDecimal weight,
        @JsonProperty("admission_reason")
        String admissionReason,
        List<String> comorbidities,
        String assessment_timestamp,
        @JsonProperty("patient_id") String patientId,
        Integer currentHour
) {
}
