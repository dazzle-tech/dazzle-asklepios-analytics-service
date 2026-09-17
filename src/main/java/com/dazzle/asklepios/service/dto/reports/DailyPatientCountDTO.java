package com.dazzle.asklepios.service.dto.reports;

import java.time.LocalDate;

public record DailyPatientCountDTO(
        LocalDate date,
        Long patientCount
) {}
