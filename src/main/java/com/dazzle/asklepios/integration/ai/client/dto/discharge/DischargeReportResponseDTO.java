package com.dazzle.asklepios.integration.ai.client.dto.discharge;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record DischargeReportResponseDTO(
        @JsonProperty("patient_id")
        String patientId,

        @JsonProperty("generation_timestamp")
        String generationTimestamp,

        @JsonProperty("generation_mode")
        String generationMode,

        @JsonProperty("report_sections")
        List<DischargeReportSectionDTO> reportSections,

        @JsonProperty("full_report_text")
        String fullReportText,

        @JsonProperty("template_used")
        Map<String, Object> templateUsed,

        @JsonProperty("confidence_score")
        Double confidenceScore,

        String disclaimer,

        @JsonProperty("requires_physician_review")
        Boolean requiresPhysicianReview
) {}
