package com.dazzle.asklepios.service.dto.reports;

import java.time.LocalDate;

public record DailyPatientEncounterCountDTO(
        LocalDate date,
        Long patientCount,
        Long encounterCount
) {}