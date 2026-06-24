package com.dazzle.asklepios.integration.ai.client.dto.medvalidation;

import java.util.List;

public record TestValidationRequestDTO(
        PatientDTO patient,
        EncounterDTO encounter,
        List<DiagnosisDTO> listOfDiagnosis,
        List<String> tests
) {}
