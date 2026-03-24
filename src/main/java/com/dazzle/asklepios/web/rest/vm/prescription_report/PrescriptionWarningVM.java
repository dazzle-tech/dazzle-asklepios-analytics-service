package com.dazzle.asklepios.web.rest.vm.prescription_report;
public record PrescriptionWarningVM(
        String type,
        String warning,
        String severity
) {}
