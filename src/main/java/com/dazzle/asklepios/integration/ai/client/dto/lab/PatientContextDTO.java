package com.dazzle.asklepios.integration.ai.client.dto.lab;

import java.util.List;

public record PatientContextDTO(
        Integer age,
        String sex,
        List<ConditionDTO> known_conditions,
        String clinical_context
) {}
