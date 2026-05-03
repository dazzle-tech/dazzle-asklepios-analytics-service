package com.dazzle.asklepios.service.dto.reports;

import com.dazzle.asklepios.service.dto.prescription.PrescriptionMedicationDTO;

import java.time.Instant;
import java.util.List;

public record VisitReportDTO(

        NurseSummaryPatientInfoDTO patientInfo,
        NurseSummaryEncounterInfoDTO encounterInfo,

        NurseSummaryObservationDTO observation,
        NurseSummaryVitalSignsDTO vitalSigns,
        NurseSummaryBodyMeasurementsDTO bodyMeasurements,
        NurseSummaryAdditionalMeasurementsDTO additionalMeasurements,

        List<NurseSummaryAllergyDTO> allergies,
        List<NurseSummaryWarningDTO> warnings,

        List<OrderedDiagnosticsDTO> diagnostics,
        List<PrescriptionMedicationDTO> medications,
        List<ProceduresDTO> procedures,
        String physicianFullName,

        Instant generatedAt
) {}