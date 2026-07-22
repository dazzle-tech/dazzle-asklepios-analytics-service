package com.dazzle.asklepios.integration.ai.controller.vm;

import java.util.List;

public record PatientRecommendationRequestVM(
        Long patientId,
        Long encounterId,
        List<String> focusAreas
) {}