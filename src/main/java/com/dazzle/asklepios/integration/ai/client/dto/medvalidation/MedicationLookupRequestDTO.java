package com.dazzle.asklepios.integration.ai.client.dto.medvalidation;

public record MedicationLookupRequestDTO(
        Long patientId,
        Long encounterId,
        Long prescriptionId
) {}
