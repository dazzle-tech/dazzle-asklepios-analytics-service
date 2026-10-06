package com.dazzle.asklepios.service.dto.reports.vaccination;

import java.time.Instant;

public record VaccinationReportDoseDTO(
        String brandName,
        String doseNumber,
        Instant dateAdministered,
        String vaccinationLocation
) {
}
