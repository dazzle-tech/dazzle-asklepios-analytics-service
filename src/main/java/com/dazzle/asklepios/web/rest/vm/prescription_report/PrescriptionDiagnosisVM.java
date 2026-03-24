package com.dazzle.asklepios.web.rest.vm.prescription_report;

import java.time.Instant;

public record PrescriptionDiagnosisVM(
        String diagnosis,
        Instant date
) {}