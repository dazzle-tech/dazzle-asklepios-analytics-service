package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.dazzle.asklepios.domain.enumeration.BloodGroup;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)

public record SepsisPatientInfoDTO(
        String name,
        Integer age,
        String gender,
        @JsonProperty("weight_kg")
        BigDecimal weighKg,
        @JsonProperty("height_cm")
        BigDecimal heightCm,
        BigDecimal bmi,
        @JsonProperty("blood_type")
        BloodGroup bloodType,
        @JsonProperty("admission_reason")
        String admissionReason,
        List<String> comorbidities,
        List<String> allergies
) {
}
