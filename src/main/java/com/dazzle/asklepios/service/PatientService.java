package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Address;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.PractitionersRepository;
import com.dazzle.asklepios.service.dto.patient.PatientInformationReportDTO;
import com.dazzle.asklepios.service.dto.patient.PatientWristbandDTO;
import com.dazzle.asklepios.service.dto.patientLabel.PatientLabelDTO;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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


    private String safe(String value) {
        return value == null ? "" : value;
    }



    @Transactional(readOnly = true)
    public PatientInformationReportDTO getPatientInformationReport(Long patientId) {

        LOG.debug("[PatientReport] GET_PATIENT_INFORMATION_REPORT start patientId={}", patientId);


        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Patient not found with id " + patientId,
                        "patient",
                        "notfound"
                ));

        String fullName = String.join(" ",
                safe(patient.getFirstName()),
                safe(patient.getSecondName()),
                safe(patient.getThirdName()),
                safe(patient.getLastName())
        ).trim();

        Integer age = null;
        if (patient.getDateOfBirth() != null) {
            age = Period.between(
                    patient.getDateOfBirth().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate(),
                    LocalDate.now()
            ).getYears();
        }

        String gender = patient.getSexAtBirth() != null
                ? patient.getSexAtBirth().name()
                : null;


        PatientDocument document = patientDocumentRepository
                .findFirstByPatientIdAndIsPrimaryTrue(patientId)
                .orElse(null);

        String documentType = document != null ? document.getType().name() : null;
        String documentNumber = document != null ? document.getNumber() : null;


        Address address = null;

        try {
            address = addressService.findCurrentByPatient(patientId);
        } catch (Exception e) {
            address = null;
        }

        String city = null;
        String state = null;
        String country = null;

        if (address != null && address.getLocationJson() != null) {

            var location = address.getLocationJson();

            if (location.getArea() != null) {
                city = location.getArea().getName();
            }

            if (location.getDistrict() != null) {
                state = location.getDistrict().getName();
            }

            if (location.getCountry() != null) {
                country = location.getCountry().getName();
            }
        }

        String cityStateCountry = Stream.of(city, state, country)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(" / "));

        String insuranceProvider = null;
        String policyNumber = null;

        PatientInsurance insurance = patientInsuranceRepository
                .findFirstByPatientIdAndIsPrimaryTrue(patientId)
                .orElse(null);

        if (insurance != null) {
            policyNumber = String.valueOf(insurance.getPolicyNumber());

            // 🔥 FIX: بدل ID → اسم الـ Payor
            if (insurance.getPayor() != null) {
                insuranceProvider = insurance.getPayor().getName(); // تأكد من field
            }
        }


        String preferredDoctor = null;

        PatientPreferredHealthProfessional preferred =
                patientPreferredHealthProfessionalRepository
                        .findFirstByPatientId(patientId)
                        .orElse(null);


        if (preferred != null) {
            Practitioner p = practitionersRepository
                    .findById(preferred.getPractitionerId())
                    .orElse(null);

            if (p != null) {
                preferredDoctor = p.getFirstName() + " " + p.getLastName();
            }
        }

        String relationship = patient.getEmergencyContactRelation();


        return new PatientInformationReportDTO(
                patient.getId(),
                fullName,
                patient.getMedicalRecordNumber(),
                patient.getDateOfBirth() != null ? patient.getDateOfBirth().toInstant() : null,
                age,
                gender,
                null,

                documentType,
                documentNumber,
                patient.getPrimaryMobileNumber(),
                patient.getSecondMobileNumber(),
                patient.getEmail(),

                city,
                state,
                country,

                patient.getEmergencyContactName(),
                relationship,
                patient.getEmergencyContactPhone(),
                patient.getCreatedDate(),
                insuranceProvider,
                policyNumber,
                preferredDoctor
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
}