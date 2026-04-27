package com.dazzle.asklepios.service.dto.reports;

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

        List<NurseSummaryServiceProductDTO> diagnostics,
        List<NurseSummaryServiceProductDTO> medications,
        List<NurseSummaryServiceProductDTO> procedures,
        String physicianFullName,

        Instant generatedAt
) {}