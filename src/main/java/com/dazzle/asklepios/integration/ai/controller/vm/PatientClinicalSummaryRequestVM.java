package com.dazzle.asklepios.integration.ai.controller.vm;

public record PatientClinicalSummaryRequestVM(
        Long patientId,
        Long encounterId
) {}