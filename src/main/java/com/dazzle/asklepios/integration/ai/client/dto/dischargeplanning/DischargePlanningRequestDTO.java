package com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DischargePlanningRequestDTO(
        @JsonProperty("request_id")
        String requestId,

        @JsonProperty("patient_context")
        PatientContextDTO patientContext,

        @JsonProperty("clinical_data")
        ClinicalDataDTO clinicalData,

        @JsonProperty("operational_data")
        OperationalDataDTO operationalData
) {}
