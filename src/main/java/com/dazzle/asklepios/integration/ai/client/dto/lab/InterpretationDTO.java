package com.dazzle.asklepios.integration.ai.client.dto.lab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterpretationDTO(
        String severity,
        List<KeyFindingDTO> key_findings,
        List<PatternDTO> patterns,
        List<TrendDTO> trends,
        List<String> follow_up_considerations
) {}