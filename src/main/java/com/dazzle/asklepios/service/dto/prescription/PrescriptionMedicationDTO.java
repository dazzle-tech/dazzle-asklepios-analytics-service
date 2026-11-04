package com.dazzle.asklepios.service.dto.prescription;

public record PrescriptionMedicationDTO(
        String activeIngredient,
        String medicationName,
        String instructions,
        Long duration,
        String durationType,
        Boolean refillAllowed,
        Long numberOfRefills,
        String administrationInstructions,
        Boolean substitutionAllowed,
        String indicationIcd,
        String notes,
        String extraDocumentation,
        String parametersToMonitor,
        String refillInterval,
        String refillsAndParameters
) {
}
