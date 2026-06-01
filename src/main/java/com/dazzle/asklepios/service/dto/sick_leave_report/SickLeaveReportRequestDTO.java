package com.dazzle.asklepios.service.dto.sick_leave_report;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Request DTO for sick leave report. Converted from Lombok VM to a Java record for immutability.
 */
public record SickLeaveReportRequestDTO(

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate fromDate,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate toDate,

        String notes
)
{
}


