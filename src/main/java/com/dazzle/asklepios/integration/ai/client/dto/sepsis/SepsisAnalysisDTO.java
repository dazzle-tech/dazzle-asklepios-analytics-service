package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record SepsisAnalysisDTO(
        String assessment_timestamp,
        @JsonProperty("patient_id") String patientId,
        String name,
        Integer age,
        String gender,
        BigDecimal weight,
        @JsonProperty("admission_reason") String admissionReason,
        List<String> comorbidities,
        String allergies,
//        @JsonProperty("patient_snapshot") SepsisPatientSnapshotDTO patientSnapshot,
        @JsonProperty("status_summary") SepsisStatusSummaryDTO statusSummary,
        @JsonProperty("current_vitals") SepsisCurrentVitalsDTO currentVitals,
        @JsonProperty("current_labs") SepsisCurrentLabsDTO currentLabs,
        @JsonProperty("timeline_analysis") List<SepsisTimelineAnalysisDTO> timelineAnalysis,
        @JsonProperty("organ_dysfunction") SepsisOrganDysfunctionDTO organDysfunction,
        List<SepsisWatchListDTO> watchList,
        @JsonProperty("risk_factors") SepsisRiskFactors riskFactors,
        @JsonProperty("forecast_24h") SepsisForcast24h forcast24h,
        @JsonProperty("sepsis_probability_24h") SepsisProbability24h sepsisProbability24h,
        @JsonProperty("recommended_actions") List<SepsisRecommendedActions> recommendedActions,
        SepsisFlags flags
) {
}
