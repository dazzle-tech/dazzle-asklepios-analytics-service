package com.dazzle.asklepios.web.rest.vm.prescription_report;

public record PrescriptionAllergyVM(
        String type,
        String allergene,
        String severity
) {}
