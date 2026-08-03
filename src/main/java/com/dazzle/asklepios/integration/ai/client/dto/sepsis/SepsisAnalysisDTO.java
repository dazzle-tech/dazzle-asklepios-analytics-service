package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record SepsisAnalysisDTO(
        @JsonProperty("patient_snapshot") SepsisPatientSnapshotDTO patientSnapshot,
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
