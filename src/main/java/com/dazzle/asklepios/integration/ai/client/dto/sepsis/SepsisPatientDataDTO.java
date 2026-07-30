package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)

public record SepsisPatientDataDTO(
        @JsonProperty("patient_info")
        SepsisPatientInfoDTO patientInfo,
        @JsonProperty("hourly_data")
        List<String> hourlyData
) {
}
