package com.dazzle.asklepios.integration.ai.client.dto.discharge;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DischargeReportRequestDTO(
        @JsonProperty("patient_record")
        PatientRecordDTO patientRecord,

        @JsonProperty("clinical_documentation")
        ClinicalDocumentationDTO clinicalDocumentation,

        @JsonProperty("report_template")
        ReportTemplateDTO reportTemplate,

        @JsonProperty("generation_mode")
        String generationMode,

        @JsonProperty("include_medications")
        boolean includeMedications,

        @JsonProperty("include_followup")
        boolean includeFollowup
) {}
