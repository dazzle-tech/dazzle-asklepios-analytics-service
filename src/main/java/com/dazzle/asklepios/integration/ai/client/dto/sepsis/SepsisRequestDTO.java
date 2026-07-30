package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SepsisRequestDTO(
        @JsonProperty("patient_data")
        SepsisPatientDataDTO patientData
){
}
