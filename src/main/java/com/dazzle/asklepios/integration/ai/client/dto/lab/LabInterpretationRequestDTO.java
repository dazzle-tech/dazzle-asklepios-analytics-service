package com.dazzle.asklepios.integration.ai.client.dto.lab;

import java.util.List;

public record LabInterpretationRequestDTO(
        String request_id,
        PatientContextDTO patient_context,
        List<LabResultDTO> lab_results,
        List<MedicationDTO> medications
) {}