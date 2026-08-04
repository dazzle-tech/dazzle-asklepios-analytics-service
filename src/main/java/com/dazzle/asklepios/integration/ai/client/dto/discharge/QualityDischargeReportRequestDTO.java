package com.dazzle.asklepios.integration.ai.client.dto.discharge;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QualityDischargeReportRequestDTO(
        @JsonProperty("discharge_report")
        String dischargeReport,

        @JsonProperty("patient_record")
        PatientRecordDTO patientRecord,

        @JsonProperty("onsite_docs")
        List<ClinicalDocumentationDTO> onsiteDocs,

        @JsonProperty("report_template")
        ReportTemplateDTO reportTemplate
) {}
