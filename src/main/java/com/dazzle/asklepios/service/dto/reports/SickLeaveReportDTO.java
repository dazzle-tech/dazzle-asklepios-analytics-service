package com.dazzle.asklepios.service.dto.reports;

import java.time.Instant;
import java.time.LocalDate;

public record SickLeaveReportDTO(

        NurseSummaryPatientInfoDTO patientInfo,
        NurseSummaryEncounterInfoDTO encounterInfo,

        String diagnosis,
        String notes,

        LocalDate sickLeaveFromDate,
        LocalDate sickLeaveToDate,
        Integer numberOfDays,

        String physicianFullName,
        String physicianSpecialty,

        Instant generatedAt
) {}
