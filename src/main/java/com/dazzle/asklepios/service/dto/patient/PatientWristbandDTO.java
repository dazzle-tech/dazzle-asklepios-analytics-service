package com.dazzle.asklepios.service.dto.patient;

import java.time.LocalDateTime;
import java.util.Date;


public record PatientWristbandDTO(

        String fullName,
        String mrn,
        Date dateOfBirth,
        String gender,

        String barcode,
        String qrCode,

        String allergyAlert,
        String bloodGroup,

        LocalDateTime admissionDateTime,
        String facilityName

) {}