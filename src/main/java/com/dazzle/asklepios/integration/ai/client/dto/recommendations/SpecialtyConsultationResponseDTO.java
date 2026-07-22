package com.dazzle.asklepios.integration.ai.client.dto.recommendations;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SpecialtyConsultationResponseDTO(
        @JsonProperty("request_id")
        String requestId,

        String summary,

        List<ConsultationActionDTO> actions
) {}