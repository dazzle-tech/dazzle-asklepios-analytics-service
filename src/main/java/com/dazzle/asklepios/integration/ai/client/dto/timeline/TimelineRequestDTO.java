package com.dazzle.asklepios.integration.ai.client.dto.timeline;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TimelineRequestDTO(
        @JsonProperty("request_id")
        String requestId,

        @JsonProperty("patient_data")
        PatientDataInputDTO patientData
) {}
