package com.dazzle.asklepios.web.rest.vm.prescription_report;

import java.time.Instant;

public record PrescriptionMedicationVM(
        Integer medicationNumber,
        String medicationName,
        String instructions,
        String duration,
        Boolean refillAllowed,
        Integer numberOfRefills,
        String administrationInstructions,
        Instant validUntil,
        Boolean substitutionAllowed
) {}