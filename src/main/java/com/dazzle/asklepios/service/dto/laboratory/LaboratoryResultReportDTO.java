package com.dazzle.asklepios.service.dto.laboratory;


import java.util.Date;
import java.util.List;

public record LaboratoryResultReportDTO(
        String facilityName,
        String departmentName,

        String patientFullName,
        String mrn,
        Date dateOfBirth,
        String age,
        String gender,
        String primaryMobileNumber,

        List<LaboratoryOrderSectionDTO> orders
) {

}