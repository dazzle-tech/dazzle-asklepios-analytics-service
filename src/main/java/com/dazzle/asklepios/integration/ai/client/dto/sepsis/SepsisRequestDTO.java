package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)

public record SepsisRequestDTO(
        @JsonProperty("patient_data")
        SepsisPatientDataDTO patientData
){
}
