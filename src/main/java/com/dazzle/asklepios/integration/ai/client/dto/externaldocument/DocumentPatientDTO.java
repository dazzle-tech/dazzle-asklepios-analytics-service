package com.dazzle.asklepios.integration.ai.client.dto.externaldocument;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DocumentPatientDTO(
        @JsonProperty("patient_id") String patientId,
        @JsonProperty("full_name") String fullName,
        String sex,
        @JsonProperty("date_of_birth") String dateOfBirth
) {}
