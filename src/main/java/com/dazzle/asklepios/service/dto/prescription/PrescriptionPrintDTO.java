package com.dazzle.asklepios.service.dto.prescription;

import com.dazzle.asklepios.domain.enumeration.EncounterReason;
import com.dazzle.asklepios.domain.enumeration.Gender;

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
        Gender gender,
        String phoneNumber,
        String email,
        String insurance,

        String encounterId,
        LocalDate encounterDateTime,
        EncounterReason encounterReason,
        String encounterDepartment,

        List<PrescriptionDiagnosisDTO> diagnoses,
        List<PrescriptionAllergyDTO> allergies,
        List<PrescriptionWarningDTO> warnings,
        List<PrescriptionMedicationDTO> medications
) {
}