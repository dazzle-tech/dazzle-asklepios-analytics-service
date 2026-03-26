package com.dazzle.asklepios.web.rest.vm.prescription_report;

import com.dazzle.asklepios.domain.enumeration.Gender;

import java.time.Instant;
import java.util.List;

public record PrescriptionReportVM(
        // Prescription
        String prescriptionNumber,
        Instant prescriptionDateTime,
        String urgency,
        String prescriberName,
        String facilityName,
        String departmentName,
        String indication,

        // Patient Information
        String patientName,
        String mrn,
        String primaryDocumentType,
        String primaryDocumentNumber,
        Instant dateOfBirth,
        String age,
        Gender gender,
        String phoneNumber,
        String email,
        String insurance,

        // Visit Information
        String encounterId,
        Instant encounterDateTime,
        String encounterReason,
        String encounterDepartment,

        // Patient Diagnosis & Allergies
        List<PrescriptionDiagnosisVM> diagnoses,
        List<PrescriptionAllergyVM> allergies,
        List<PrescriptionWarningVM> warnings,

        // Medications of the Prescription
        List<PrescriptionMedicationVM> medications
) {}