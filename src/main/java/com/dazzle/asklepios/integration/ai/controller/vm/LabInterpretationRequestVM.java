package com.dazzle.asklepios.integration.ai.controller.vm;

import java.time.Instant;

public record LabInterpretationRequestVM(
        Long patientId,
        Instant dateFrom,
        Instant dateTo
) {}