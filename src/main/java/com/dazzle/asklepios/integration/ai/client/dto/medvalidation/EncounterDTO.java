package com.dazzle.asklepios.integration.ai.client.dto.medvalidation;

public record EncounterDTO(
        String visitId,
        String visitType,
        String plannedStartDate,
        String chiefComplaint,
        String patientAge,
        String primaryDiagnosis
) {}
