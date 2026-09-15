package com.dazzle.asklepios.integration.ai.client.dto.discharge;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DischargeReportSectionDTO(
        @JsonProperty("section_name")
        String sectionName,

        String content,
        Double confidence,
        List<String> sources
) {}
