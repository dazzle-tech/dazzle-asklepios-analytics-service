package com.dazzle.asklepios.service.dto.prescription;

import com.dazzle.asklepios.domain.enumeration.AllergenTypes;

public record PrescriptionAllergyDTO(
        AllergenTypes type,
        String allergene,
        String severity
) {}