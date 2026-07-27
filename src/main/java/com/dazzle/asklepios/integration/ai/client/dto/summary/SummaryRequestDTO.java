package com.dazzle.asklepios.integration.ai.client.dto.summary;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SummaryRequestDTO(
        @JsonProperty("request_id")
        String requestId,

        @JsonProperty("patient_data")
        SummaryPatientDataDTO patientData
) {}