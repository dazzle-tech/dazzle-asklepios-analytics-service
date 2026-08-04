package com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PatientContextDTO(
        @JsonProperty("patient_id")
        String patientId,

        @JsonProperty("admission_date")
        String admissionDate,

        @JsonProperty("primary_diagnosis")
        String primaryDiagnosis,

        @JsonProperty("secondary_diagnoses")
        List<String> secondaryDiagnoses,

        @JsonProperty("current_status")
        String currentStatus
) {}
