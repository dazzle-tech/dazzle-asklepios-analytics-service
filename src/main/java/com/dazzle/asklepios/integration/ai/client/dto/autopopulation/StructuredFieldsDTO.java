package com.dazzle.asklepios.integration.ai.client.dto.autopopulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StructuredFieldsDTO(
        @JsonProperty("chief_complaint") String chiefComplaint,
        @JsonProperty("history_of_present_illness") String historyOfPresentIllness,
        List<String> diagnosis,
        List<Map<String, Object>> medications,
        Map<String, Object> vitals,
        List<String> procedures,
        List<String> allergies,
        String assessment,
        String plan,
        @JsonProperty("past_medical_history") String pastMedicalHistory,
        @JsonProperty("family_history") String familyHistory,
        @JsonProperty("social_history") String socialHistory,
        @JsonProperty("review_of_systems") Map<String, String> reviewOfSystems
) {}
