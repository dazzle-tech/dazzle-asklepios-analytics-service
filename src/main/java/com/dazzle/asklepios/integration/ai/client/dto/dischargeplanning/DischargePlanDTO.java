package com.dazzle.asklepios.integration.ai.client.dto.dischargeplanning;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DischargePlanDTO(
        @JsonProperty("readiness_status")
        String readinessStatus,

        @JsonProperty("readiness_reason")
        String readinessReason,

        List<DischargeBlockerDTO> blockers,

        @JsonProperty("medication_reconciliation_concerns")
        List<String> medicationReconciliationConcerns,

        @JsonProperty("follow_up_considerations")
        List<String> followUpConsiderations,

        @JsonProperty("draft_discharge_summary")
        String draftDischargeSummary,

        String disclaimer
) {}
