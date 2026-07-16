package com.dazzle.asklepios.integration.ai.client.dto.autopopulation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserContextDTO(
        @JsonProperty("user_id") String userId,
        @JsonProperty("user_role") String userRole,
        String department
) {}
