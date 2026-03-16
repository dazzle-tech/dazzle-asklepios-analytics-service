package com.dazzle.asklepios.web.rest.vm;

import java.util.Date;

public record PatientLabelVM(
        Long patientId,
                String patientFullName,
                String mrn,
                Date dateOfBirth,
                Integer age,
                String gender,
                Date registrationDate
) {}
