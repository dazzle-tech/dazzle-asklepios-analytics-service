package com.dazzle.asklepios.service.dto.reports.dailyPatientVisit;

import java.time.LocalDate;


public record DailyPatientVisitDTO (

     String firstName,
     String lastName,
     String medicalRecordNumber,
     String encounterNumber,
     LocalDate visitDate,
     String departmentName,
     String practitionerFirstName,
     String practitionerLastName
){
}
