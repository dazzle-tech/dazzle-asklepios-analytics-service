package com.dazzle.asklepios.integration.ai.client.dto.discharge;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record PatientRecordDTO(
        @JsonProperty("patient_id")
        String patientId,

        Integer age,
        String gender,

        @JsonProperty("admission_date")
        String admissionDate,

        @JsonProperty("discharge_date")
        String dischargeDate,

        @JsonProperty("primary_diagnosis")
        String primaryDiagnosis,

        @JsonProperty("secondary_diagnoses")
        List<String> secondaryDiagnoses,

        List<String> allergies,

        @JsonProperty("medications_on_admission")
        List<Map<String, String>> medicationsOnAdmission
) {}
