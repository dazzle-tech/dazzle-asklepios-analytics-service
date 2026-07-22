package com.dazzle.asklepios.integration.ai.controller.vm;

public record PatientSpecialtyConsultationRequestVM(
        Long patientId,
        Long encounterId,
        String specialty
) {}