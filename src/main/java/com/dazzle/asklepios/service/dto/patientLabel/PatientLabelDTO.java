package com.dazzle.asklepios.service.dto.patientLabel;

import java.util.Date;

public record PatientLabelDTO(
        Long patientId,
        String patientFullName,
        String mrn,
        Date dateOfBirth,
        Integer age,
        String gender,
        Date registrationDate
) {}