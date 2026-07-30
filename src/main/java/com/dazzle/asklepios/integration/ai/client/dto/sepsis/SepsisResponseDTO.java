package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SepsisResponseDTO(
        @JsonProperty("patient_id")
        String patientId,
        SepsisAnalysisDTO analysis,
        @JsonProperty("output_file")
        String outputFile,
        String source
) {
}
