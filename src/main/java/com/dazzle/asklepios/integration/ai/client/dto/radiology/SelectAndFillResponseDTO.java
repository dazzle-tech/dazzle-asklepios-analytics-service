package com.dazzle.asklepios.integration.ai.client.dto.radiology;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SelectAndFillResponseDTO(

        @JsonProperty("ID")
        Long id,

        @JsonProperty("TEMPLATE_NAME")
        String templateName,

        @JsonProperty("TEMPLATE_TEXT")
        String templateText,

        @JsonProperty("STATUS_ID")
        Integer statusId,

        @JsonProperty("CREATED_BY")
        String createdBy,

        @JsonProperty("CREATION_DATETIME")
        String creationDatetime,

        @JsonProperty("DELETED_BY")
        String deletedBy,

        @JsonProperty("DELETE_DATETIME")
        Instant deleteDatetime,

        @JsonProperty("UPDATED_BY")
        String updatedBy,

        @JsonProperty("UPDATE_DATETIME")
        String updateDatetime,

        @JsonProperty("Physician")
        String physician

) {
}