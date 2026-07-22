package com.dazzle.asklepios.integration.ai.client.dto.recommendations;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpecialtyConsultationRequestDTO(
        @JsonProperty("request_id")
        String requestId,

        String specialty,

        @JsonProperty("patient_context")
        PatientContextDTO patientContext,

        String complaint
) {}