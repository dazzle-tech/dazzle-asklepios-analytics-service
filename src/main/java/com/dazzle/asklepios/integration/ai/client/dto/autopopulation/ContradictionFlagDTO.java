package com.dazzle.asklepios.integration.ai.client.dto.autopopulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ContradictionFlagDTO(
        @JsonProperty("field_name") String fieldName,
        @JsonProperty("user_text_value") Object userTextValue,
        @JsonProperty("patient_record_value") Object patientRecordValue,
        String recommendation,
        String severity
) {}
