package com.dazzle.asklepios.web.rest.vm.patient;

import com.dazzle.asklepios.domain.Patient;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class PatientBasicInformationResponseVM {
    private Long id;
    private String firstName;
    private String lastName;
    private String medicalRecordNumber;
    private Date dateOfBirth;
    private String sexAtBirth;

    public static PatientBasicInformationResponseVM ofEntity(Patient patient) {
        if (patient == null) {
            return null;
        }

        return PatientBasicInformationResponseVM.builder()
                .id(patient.getId())
                .medicalRecordNumber(patient.getMedicalRecordNumber())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .sexAtBirth(patient.getSexAtBirth())
                .dateOfBirth(patient.getDateOfBirth())
                .build();
    }
}
