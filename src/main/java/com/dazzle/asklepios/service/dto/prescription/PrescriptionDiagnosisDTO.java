package com.dazzle.asklepios.service.dto.prescription;

import java.time.Instant;

public record PrescriptionDiagnosisDTO(
        String diagnosis,

        Instant date
) {}