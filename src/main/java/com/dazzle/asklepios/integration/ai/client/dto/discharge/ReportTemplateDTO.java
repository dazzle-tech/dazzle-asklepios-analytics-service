package com.dazzle.asklepios.integration.ai.client.dto.discharge;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ReportTemplateDTO(
        @JsonProperty("template_name")
        String templateName,

        List<String> sections,

        @JsonProperty("required_fields")
        List<String> requiredFields,

        @JsonProperty("format_type")
        String formatType
) {}
