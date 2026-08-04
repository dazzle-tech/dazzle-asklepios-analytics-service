package com.dazzle.asklepios.integration.ai.client.dto.discharge;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record QualityDischargeReportResponseDTO(
        @JsonProperty("qa_method")
        String qaMethod,

        @JsonProperty("overall_score")
        Double overallScore,

        String summary,

        @JsonProperty("parsed_report")
        Map<String, Object> parsedReport,

        List<Map<String, Object>> errors,

        @JsonProperty("missing_items")
        List<Map<String, Object>> missingItems,

        List<Map<String, Object>> inconsistencies,

        @JsonProperty("recommended_corrections")
        List<Map<String, Object>> recommendedCorrections
) {}
