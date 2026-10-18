package com.dazzle.asklepios.service.dto.prescription;


import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public record PrescriptionPrintDTO(
        String prescriptionNumber,
        Instant prescriptionDateTime,
        String prescriberName,
        String prescriberEmail,
        String facilityName,
        String fromDepartment,
        String patientName,
        String mrn,
        String primaryDocumentType,
        String primaryDocumentNumber,
        Date dateOfBirth,
        String age,
        String gender,
        String phoneNumber,
        String email,
        String encounterId,
        LocalDate encounterDateTime,
        String encounterReason,
        String encounterDepartment,

        List<PrescriptionDiagnosisDTO> diagnoses,
        List<PrescriptionAllergyDTO> allergies,
        List<PrescriptionWarningDTO> warnings,
        List<PrescriptionMedicationDTO> medications
) {
}