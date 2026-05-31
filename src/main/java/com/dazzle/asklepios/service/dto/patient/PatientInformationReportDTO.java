package com.dazzle.asklepios.service.dto.patient;

import java.time.Instant;
public record PatientInformationReportDTO(

        Long patientId,
        String fullName,
        String mrn,
        Instant dateOfBirth,
        String age,
        String gender,
        String photoUrl,

        String documentType,
        String documentNumber,

        String mobileNumber,
        String secondaryPhone,
        String email,
        String city,
        String state,
        String country,

        String emergencyName,
        String emergencyRelationship,
        String emergencyPhone,

        Instant registrationDate,
        String insuranceProvider,
        String policyNumber,

        String preferredHealthProfessional

) {}