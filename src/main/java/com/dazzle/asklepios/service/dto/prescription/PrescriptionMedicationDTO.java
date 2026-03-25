package com.dazzle.asklepios.service.dto.prescription;

import java.time.LocalDate;

public record PrescriptionMedicationDTO(
        String medicationName,
        String instructions,
        Long duration,
        Boolean refillAllowed,
        Long numberOfRefills,
        String administrationInstructions,
        LocalDate validUntil,
        Boolean substitutionAllowed,
        String indicationIcd
) {
}