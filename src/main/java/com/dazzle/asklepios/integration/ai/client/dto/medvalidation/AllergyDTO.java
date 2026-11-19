package com.dazzle.asklepios.integration.ai.client.dto.medvalidation;

import java.util.List;

public record AllergyDTO(
        String allergenName,
        String allergenType,
        String severity,
        String criticality,
        List<String> allergicReactions,
        String status
) {}
