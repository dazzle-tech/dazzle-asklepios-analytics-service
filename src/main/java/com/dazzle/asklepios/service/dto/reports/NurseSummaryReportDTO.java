package com.dazzle.asklepios.service.dto.reports;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record NurseSummaryReportDTO(

        NurseSummaryPatientInfoDTO patientInfo,
        NurseSummaryEncounterInfoDTO encounterInfo,

        NurseSummaryObservationDTO observation,
        NurseSummaryVitalSignsDTO vitalSigns,
        NurseSummaryBodyMeasurementsDTO bodyMeasurements,
        NurseSummaryAdditionalMeasurementsDTO additionalMeasurements,

        List<NurseSummaryAllergyDTO> allergies,
        List<NurseSummaryWarningDTO> warnings,
        List<NurseSummaryVaccinationDTO> vaccinations,
        List<NurseSummaryServiceProductDTO> servicesAndProducts,

        Instant generatedAt
) {
}