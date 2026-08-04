package com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClinicalDataDTO(
        @JsonProperty("latest_vitals")
        List<VitalReadingDTO> latestVitals,

        @JsonProperty("pending_tests")
        List<PendingTestDTO> pendingTests,

        @JsonProperty("active_problems")
        List<String> activeProblems,

        @JsonProperty("medications_current")
        List<String> medicationsCurrent,

        @JsonProperty("medications_planned_for_discharge")
        List<String> medicationsPlannedForDischarge,

        List<ClinicalNoteDTO> notes
) {}
