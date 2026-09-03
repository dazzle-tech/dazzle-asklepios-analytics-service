package com.dazzle.asklepios.service;


import com.dazzle.asklepios.domain.Address;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientDocument;
import com.dazzle.asklepios.domain.PatientPreferredHealthProfessional;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.repository.AddressRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.PatientAllergyRepository;
import com.dazzle.asklepios.repository.PatientDocumentRepository;
import com.dazzle.asklepios.repository.PatientPreferredHealthProfessionalRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.PractitionersRepository;
import com.dazzle.asklepios.security.SecurityUtils;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Transactional
public class PatientService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;
    private final PatientAllergyRepository patientAllergyRepository;
    private final PractitionersRepository practitionersRepository;
    private final PatientPreferredHealthProfessionalRepository patientPreferredHealthProfessionalRepository;
    private final PatientDocumentRepository patientDocumentRepository;
    private final AddressRepository addressRepository;

    public PatientService(
            PatientRepository patientRepository, FacilityRepository facilityRepository, PatientAllergyRepository patientAllergyRepository, PractitionersRepository practitionersRepository, PatientPreferredHealthProfessionalRepository patientPreferredHealthProfessionalRepository, PatientDocumentRepository patientDocumentRepository, AddressRepository addressRepository

    ) {
        this.patientRepository = patientRepository;
        this.facilityRepository = facilityRepository;
        this.patientAllergyRepository = patientAllergyRepository;
        this.practitionersRepository = practitionersRepository;
        this.patientPreferredHealthProfessionalRepository = patientPreferredHealthProfessionalRepository;
        this.patientDocumentRepository = patientDocumentRepository;
        this.addressRepository = addressRepository;
    }

    private String calculateAge(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }

        LocalDate birthDate = dateOfBirth.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate today = LocalDate.now();
        Period period = Period.between(birthDate, today);

        return period.getYears() + " Years " +
                period.getMonths() + " Months " +
                period.getDays() + " Days";
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

        String fullName = Stream.of(
                        patient.getFirstName(),
                        patient.getSecondName(),
                        patient.getThirdName(),
                        patient.getLastName()
                )
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(" "));

        List<PatientAllergies> allergies =
                Optional.ofNullable(patientAllergyRepository.findAllByPatientId(patient.getId()))
                        .orElse(Collections.emptyList());

        String allergy = allergies == null || allergies.isEmpty()
                ? "No Allergy"
                : allergies.stream()
                .map(a -> a.getAllergen() != null ? a.getAllergen().getName() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
        String bloodGroup = patient.getBloodGroup();
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

    @Transactional(readOnly = true)
    public PatientInformationReportDTO getPatientInformationReport(Long patientId) {

        LOG.debug("[PatientReport] GET_PATIENT_INFORMATION_REPORT start patientId={}", patientId);

        /* ===================== 1. Patient ===================== */

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

        String age = calculateAge(patient.getDateOfBirth());

        String gender = patient.getSexAtBirth() != null
                ? patient.getSexAtBirth().name()
                : null;

        /* ===================== 2. Document ===================== */

        PatientDocument document = patientDocumentRepository
                .findFirstByPatientIdAndIsPrimaryTrue(patientId)
                .orElse(null);

        String documentType = document != null ? document.getType().name() : null;
        String documentNumber = document != null ? document.getNumber() : null;

        /* ===================== 3. Address ===================== */

        /* ===================== 3. Address ===================== */


        Address address = addressRepository
                .findFirstByPatientIdAndIsCurrentTrueOrderByIdDesc(patientId)
                .orElse(null);
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
        /* ===================== 4. Insurance ===================== */




        /* ===================== 5. Preferred Doctor ===================== */


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

        /* ===================== 6. Emergency Contact ===================== */

        String relationship = patient.getEmergencyContactRelation();


        /* ===================== 7. DTO ===================== */

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
                preferredDoctor
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}