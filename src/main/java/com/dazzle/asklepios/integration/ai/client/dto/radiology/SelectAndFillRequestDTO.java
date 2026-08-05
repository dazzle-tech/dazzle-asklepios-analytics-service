package com.dazzle.asklepios.integration.ai.client.dto.radiology;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
public record SelectAndFillRequestDTO(

        @JsonProperty("request_id")
        String requestId,

        @JsonProperty("input_data")
        String inputData,

        @JsonProperty("use_static_template")
        Boolean useStaticTemplate,

        @JsonProperty("static_template_id")
        String staticTemplateId,

        @JsonProperty("top_k")
        Integer topK,

        @JsonProperty("patient_context")
        Map<String, Object> patientContext,

        @JsonProperty("OutputLanguage")
        String outputLanguage

) {
}