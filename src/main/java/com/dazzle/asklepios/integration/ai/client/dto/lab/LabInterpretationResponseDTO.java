package com.dazzle.asklepios.integration.ai.client.dto.lab;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LabInterpretationResponseDTO(
        String request_id,
        String summary,
        InterpretationDTO interpretation
) {}

