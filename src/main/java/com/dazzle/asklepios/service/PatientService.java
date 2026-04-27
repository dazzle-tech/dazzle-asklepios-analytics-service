package com.dazzle.asklepios.service;


import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.service.dto.patient.PatientWristbandDTO;
import com.dazzle.asklepios.service.dto.patientLabel.PatientLabelDTO;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PatientService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;


    public PatientService(
            PatientRepository patientRepository

    ) {
        this.patientRepository = patientRepository;
    }


    // TODO move this logic to analytic service
    @Transactional(readOnly = true)
    public PatientLabelDTO getPatientLabel(Long patientId) {

        LOG.debug("[PatientLabelService] GET_PATIENT_LABEL start patientId={}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Patient not found with id " + patientId,
                        "patient",
                        "notfound"
                ));

        String fullName = (
                (patient.getFirstName() != null ? patient.getFirstName() : "") + " " +
                        (patient.getSecondName() != null ? patient.getSecondName() : "") + " " +
                        (patient.getThirdName() != null ? patient.getThirdName() : "") + " " +
                        (patient.getLastName() != null ? patient.getLastName() : "")
        ).trim();

        Integer age = null;

        if (patient.getDateOfBirth() != null) {
            age = java.time.Period.between(
                    patient.getDateOfBirth().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    java.time.LocalDate.now()
            ).getYears();
        }

        return new PatientLabelDTO(
                patient.getId(),
                fullName,
                patient.getMedicalRecordNumber(),
                patient.getDateOfBirth(),
                age,
                patient.getSexAtBirth() != null ? patient.getSexAtBirth().name() : null,
                patient.getCreatedDate() != null ? java.util.Date.from(patient.getCreatedDate()) : null
        );
    }
    @Transactional(readOnly = true)
    public PatientWristbandDTO getPatientWristband(Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundAlertException("Patient not found", "patient", "notfound"));

        String fullName = (patient.getFirstName() + " " + patient.getLastName()).trim();

        // TODO: replace with real data
        String allergy = "No Allergy";
        String bloodGroup = "O+";
        LocalDateTime admission = LocalDateTime.now();
        String facility = "Asklepios Hospital";

        return new PatientWristbandDTO(
                fullName,
                patient.getMedicalRecordNumber(),
                patient.getDateOfBirth(),
                patient.getSexAtBirth() != null ? patient.getSexAtBirth().name() : null,

                patient.getMedicalRecordNumber(), // barcode
                patient.getId().toString(),       // QR

                allergy,
                bloodGroup,
                admission,
                facility
        );
    }

}