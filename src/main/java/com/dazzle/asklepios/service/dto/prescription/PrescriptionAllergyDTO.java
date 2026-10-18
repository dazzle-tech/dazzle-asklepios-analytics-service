package com.dazzle.asklepios.service.dto.prescription;


public record PrescriptionAllergyDTO(
        String type,
        String allergene,
        String severity
) {}