package com.dazzle.asklepios.service.dto.prescription;

import java.time.LocalDate;

public record PrescriptionMedicationDTO(
        String activeIngredient ,
        String medicationName,
        String instructions,
        Long duration,
        Boolean refillAllowed,
        Long numberOfRefills,
        String administrationInstructions,
        Boolean substitutionAllowed,
        String indicationIcd
) {
}