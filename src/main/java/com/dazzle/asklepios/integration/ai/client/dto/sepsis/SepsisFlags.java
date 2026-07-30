package com.dazzle.asklepios.integration.ai.client.dto.sepsis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)

public record SepsisFlags(
        @JsonProperty("escalate_care") Boolean escalateCare,
        @JsonProperty("repeat_labs_needed") Boolean repeatLabsNeeded,
        @JsonProperty("imaging_recommended") Boolean imagingRecommended,
        @JsonProperty("culture_recommended") Boolean cultureRecommended,
        @JsonProperty("antibiotic_consideration") Boolean antibioticConsideration,
        @JsonProperty("fluid_resuscitation_needed") Boolean fluidResuscitationNeeded,
        @JsonProperty("vasopressor_consideration") Boolean vasopressorConsideration,
        @JsonProperty("icu_transfer_recommended") Boolean icuTransferRecommended

        ) {
}
