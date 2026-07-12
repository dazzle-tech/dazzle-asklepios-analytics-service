package com.dazzle.asklepios.integration.ai.client.dto.medvalidation;

public record PatientDTO(
        String mrn,
        String fullName,
        String gender,
        String dob
) {}
