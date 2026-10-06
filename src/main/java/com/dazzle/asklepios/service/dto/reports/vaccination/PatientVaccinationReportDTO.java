package com.dazzle.asklepios.service.dto.reports.vaccination;

import java.util.List;

public record PatientVaccinationReportDTO(
        Long patientId,
        String patientName,
        String mrn,
        String gender,
        String dateOfBirth,
        String age,
        List<VaccinationReportVaccineDTO> vaccines
) {
}
