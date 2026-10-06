package com.dazzle.asklepios.service.dto.reports.vaccination;

import java.util.List;

public record VaccinationReportVaccineDTO(
        Long vaccineId,
        String vaccineName,
        String atcCode,
        String type,
        String numberOfDoses,
        String roa,
        String siteOfAdministration,
        List<VaccinationReportDoseDTO> doses
) {
}
