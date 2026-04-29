package com.dazzle.asklepios.service;


import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.PatientAllergyRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.dto.patient.PatientWristbandDTO;
import com.dazzle.asklepios.service.dto.patientLabel.PatientLabelDTO;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionAllergyDTO;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;
    private final PatientAllergyRepository patientAllergyRepository;

    public PatientService(
            PatientRepository patientRepository, FacilityRepository facilityRepository, PatientAllergyRepository patientAllergyRepository

    ) {
        this.patientRepository = patientRepository;
        this.facilityRepository = facilityRepository;
        this.patientAllergyRepository = patientAllergyRepository;
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

        String fullName = (patient.getFirstName().trim() +" "+ patient.getSecondName().trim()+" "+patient.getThirdName().trim()+" " + patient.getLastName()).trim();

        // TODO: replace with real data
        List<PatientAllergies> allergies =
                Optional.ofNullable(patientAllergyRepository.findAllByPatientId(patient.getId()))
                        .orElse(Collections.emptyList());

        String allergy = allergies == null || allergies.isEmpty()
                ? "No Allergy"
                : allergies.stream()
                .map(a -> a.getAllergen() != null ? a.getAllergen().getName() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
        String bloodGroup = " ";
        LocalDateTime admission = LocalDateTime.now();
        Facility facility = facilityRepository.getById(
                SecurityUtils.getCurrentUserFacility()
                        .orElseThrow(() -> new RuntimeException("No facility"))
        );
        String facilityName = facility.getName();
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
                facilityName
        );
    }

}