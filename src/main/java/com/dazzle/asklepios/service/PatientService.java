package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Address;
import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientDocument;
import com.dazzle.asklepios.domain.PatientInsurance;
import com.dazzle.asklepios.domain.PatientPreferredHealthProfessional;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.repository.DuplicationCandidateRepository;
import com.dazzle.asklepios.repository.PatientDocumentRepository;
import com.dazzle.asklepios.repository.PatientInsuranceRepository;
import com.dazzle.asklepios.repository.PatientPreferredHealthProfessionalRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.service.dto.patient.PatientWristbandDTO;
import com.dazzle.asklepios.service.dto.patientLabel.PatientLabelDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PatientService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final PatientDocumentRepository patientDocumentRepository;
    private final DuplicationCandidateRepository duplicationCandidateRepository;
    private final AddressService addressService;
    private final PatientInsuranceRepository patientInsuranceRepository;
    private final PatientPreferredHealthProfessionalRepository patientPreferredHealthProfessionalRepository;
    private final PractitionersRepository practitionersRepository;


    public PatientService(
            PatientRepository patientRepository,
            PatientDocumentRepository patientDocumentRepository,
            DuplicationCandidateRepository duplicationCandidateRepository,
            PatientInsuranceRepository patientInsuranceRepository,
            PatientPreferredHealthProfessionalRepository patientPreferredHealthProfessionalRepository,
            PractitionersRepository practitionersRepository,
            AddressService addressService


    ) {
        this.patientRepository = patientRepository;
        this.patientDocumentRepository = patientDocumentRepository;
        this.duplicationCandidateRepository = duplicationCandidateRepository;
        this.patientInsuranceRepository = patientInsuranceRepository;
        this.patientPreferredHealthProfessionalRepository = patientPreferredHealthProfessionalRepository;
        this.practitionersRepository = practitionersRepository;
        this.addressService = addressService; // 👈

    }

    private String resolveRelationshipDisplay(String code) {
        try {

            return code;

        } catch (Exception e) {
            LOG.warn("Failed to resolve relationship display for code={}", code);
            return code;
        }
    }

    public Patient create(PatientCreateDTO dto) {
        LOG.info("[CREATE] Request to create Patient payload={}", dto);

        boolean verified = Boolean.TRUE.equals(dto.isVerified());
        boolean completed = Boolean.TRUE.equals(dto.isCompletedPatient());

        Patient entity = Patient.builder()
                .firstName(dto.firstName())
                .secondName(dto.secondName())
                .thirdName(dto.thirdName())
                .lastName(dto.lastName())
                .sexAtBirth(dto.sexAtBirth())
                .dateOfBirth(dto.dateOfBirth())
                .patientClasses(dto.patientClasses())
                .isPrivatePatient(dto.isPrivatePatient())
                .firstNameSecondaryLang(dto.firstNameSecondaryLang())
                .secondNameSecondaryLang(dto.secondNameSecondaryLang())
                .thirdNameSecondaryLang(dto.thirdNameSecondaryLang())
                .lastNameSecondaryLang(dto.lastNameSecondaryLang())
                .primaryMobileNumber(dto.primaryMobileNumber())
                .secondMobileNumber(dto.secondMobileNumber())
                .homePhone(dto.homePhone())
                .workPhone(dto.workPhone())
                .email(dto.email())
                .receiveSms(dto.receiveSms())
                .receiveEmail(dto.receiveEmail())
                .preferredWayOfContact(dto.preferredWayOfContact())
                .nativeLanguage(dto.nativeLanguage())
                .emergencyContactName(dto.emergencyContactName())
                .emergencyContactRelation(dto.emergencyContactRelation())
                .emergencyContactPhone(dto.emergencyContactPhone())
                .role(dto.role())
                .maritalStatus(dto.maritalStatus())
                .nationality(dto.nationality())
                .religion(dto.religion())
                .ethnicity(dto.ethnicity())
                .occupation(dto.occupation())
                .responsibleParty(dto.responsibleParty())
                .educationalLevel(dto.educationalLevel())
                .previousId(dto.previousId())
                .archivingNumber(dto.archivingNumber())
                .details(dto.details())
                .isUnknown(false)
                .isVerified(verified)
                .isCompletedPatient(completed)
                .securityAccessLevel(dto.securityAccessLevel())
                .build();

        try {
            return patientRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            handleConstraintsOnCreateOrUpdate(ex);
            throw new BadRequestAlertException(
                    "Database constraint violated while saving patient (check required fields or unique constraints).",
                    "patient",
                    "db.constraint"
            );
        }
    }

    public Patient createUnknown(UnknownPatientCreateDTO dto) {

        Patient unknownPatient = Patient.builder()
                .isUnknown(true)
                .isVerified(Boolean.TRUE.equals(dto.isVerified()))
                .isCompletedPatient(Boolean.TRUE.equals(dto.isCompletedPatient()))
                .build();

        try {
            Patient createdPatient = patientRepository.saveAndFlush(unknownPatient);

            String mrn = createdPatient.getMedicalRecordNumber();

            if (mrn != null && !mrn.isBlank()) {
                createdPatient.setFirstName("Unknown " + mrn);
                createdPatient.setLastName(null);
                createdPatient = patientRepository.saveAndFlush(createdPatient);
            }

            LOG.info("Created UNKNOWN patient id={} MRN={}",
                    createdPatient.getId(),
                    createdPatient.getMedicalRecordNumber());

            return createdPatient;

        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            handleConstraintsOnCreateOrUpdate(ex);
            throw new BadRequestAlertException(
                    "Database constraint violated while saving patient.",
                    "patient",
                    "db.constraint"
            );
        }
    }

    public Patient update(Long id, PatientUpdateDTO dto) {
        LOG.info("[UPDATE] Request to update Patient id={} payload={}", id, dto);

        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Patient not found with id={}", id);
                    return new NotFoundAlertException(
                            "Patient not found with id " + id,
                            "patient",
                            "notfound"
                    );
                });

        existing.setFirstName(dto.firstName());
        existing.setSecondName(dto.secondName());
        existing.setThirdName(dto.thirdName());
        existing.setLastName(dto.lastName());
        existing.setSexAtBirth(dto.sexAtBirth());
        existing.setDateOfBirth(dto.dateOfBirth());
        existing.setPatientClasses(dto.patientClasses());
        existing.setIsPrivatePatient(dto.isPrivatePatient());
        existing.setFirstNameSecondaryLang(dto.firstNameSecondaryLang());
        existing.setSecondNameSecondaryLang(dto.secondNameSecondaryLang());
        existing.setThirdNameSecondaryLang(dto.thirdNameSecondaryLang());
        existing.setLastNameSecondaryLang(dto.lastNameSecondaryLang());
        existing.setPrimaryMobileNumber(dto.primaryMobileNumber());
        existing.setSecondMobileNumber(dto.secondMobileNumber());
        existing.setHomePhone(dto.homePhone());
        existing.setWorkPhone(dto.workPhone());
        existing.setEmail(dto.email());
        existing.setReceiveSms(dto.receiveSms());
        existing.setReceiveEmail(dto.receiveEmail());
        existing.setPreferredWayOfContact(dto.preferredWayOfContact());
        existing.setNativeLanguage(dto.nativeLanguage());
        existing.setEmergencyContactName(dto.emergencyContactName());
        existing.setEmergencyContactRelation(dto.emergencyContactRelation());
        existing.setEmergencyContactPhone(dto.emergencyContactPhone());
        existing.setRole(dto.role());
        existing.setMaritalStatus(dto.maritalStatus());
        existing.setNationality(dto.nationality());
        existing.setReligion(dto.religion());
        existing.setEthnicity(dto.ethnicity());
        existing.setOccupation(dto.occupation());
        existing.setResponsibleParty(dto.responsibleParty());
        existing.setEducationalLevel(dto.educationalLevel());
        existing.setPreviousId(dto.previousId());
        existing.setArchivingNumber(dto.archivingNumber());
        existing.setDetails(dto.details());
        existing.setIsUnknown(Boolean.TRUE.equals(dto.isUnknown()));
        existing.setIsVerified(Boolean.TRUE.equals(dto.isVerified()));
        existing.setIsCompletedPatient(Boolean.TRUE.equals(dto.isCompletedPatient()));
        existing.setSecurityAccessLevel(dto.securityAccessLevel());
        existing.setLastModifiedDate(Instant.now());

        try {
            Patient updatedPatient = patientRepository.saveAndFlush(existing);
            LOG.info(
                    "Successfully updated patient id={} (medicalRecordNumber='{}')",
                    updatedPatient.getId(), updatedPatient.getMedicalRecordNumber()
            );
            return updatedPatient;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            LOG.error(
                    "Database constraint violation while updating patient id={}: {}",
                    id, exception.getMessage(), exception
            );
            handleConstraintsOnCreateOrUpdate(exception);
            throw new BadRequestAlertException(
                    "Database constraint violated while updating patient (check required fields or unique constraints).",
                    "patient",
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<Patient> findByMedicalRecordNumber(String medicalRecordNumber, Pageable pageable) {
        LOG.debug("[FIND BY medicalRecordNumber] Searching patients by medicalRecordNumber='{}' pageable={}", medicalRecordNumber, pageable);
        return patientRepository.findByMedicalRecordNumberContainingIgnoreCase(medicalRecordNumber, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Patient> findByArchivingNumber(String archivingNumber, Pageable pageable) {
        LOG.debug("[FIND BY ARCHIVING] Searching patients by archivingNumber='{}' pageable={}", archivingNumber, pageable);
        return patientRepository.findByArchivingNumberContainingIgnoreCase(archivingNumber, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Patient> findByPrimaryPhone(String primaryPhone, Pageable pageable) {
        LOG.debug("[FIND BY PHONE] Searching patients by primaryPhone='{}' pageable={}", primaryPhone, pageable);
        return patientRepository.findByPrimaryMobileNumberContaining(primaryPhone, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Patient> findByDateOfBirth(LocalDate dateOfBirth, Pageable pageable) {
        LOG.debug("[FIND BY DOB] Searching patients by dateOfBirth={} pageable={}", dateOfBirth, pageable);
        return patientRepository.findByDateOfBirth(dateOfBirth, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Patient> findByFullName(String keyword, Pageable pageable) {
        LOG.debug("[FIND BY NAME] Searching patients by keyword='{}' pageable={}", keyword, pageable);
        return patientRepository
                .findByFirstNameContainingIgnoreCaseOrSecondNameContainingIgnoreCaseOrThirdNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        keyword, keyword, keyword, keyword, pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Patient> findUnknownPatients(Pageable pageable) {
        LOG.debug("[FIND UNKNOWN] Fetching unknown patients with pageable={}", pageable);
        return patientRepository.findByIsUnknownTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Patient> findByPrimaryDocumentNumber(String numberPart, Pageable pageable) {
        LOG.debug("[FIND BY PRIMARY DOCUMENT] numberPart='{}' pageable={}", numberPart, pageable);
        Page<PatientDocument> docsPage =
                patientDocumentRepository.findByIsPrimaryTrueAndNumberContainingIgnoreCase(numberPart, pageable);
        return docsPage.map(PatientDocument::getPatient);
    }

    @Transactional(readOnly = true)
    public List<Patient> findByIds(List<Long> ids) {
        LOG.debug("[BULK FIND] Fetching Patients by ids count={} ids={}", ids.size(), ids);
        List<Patient> patients = patientRepository.findAllById(ids);
        LOG.debug("[BULK FIND] Found Patients count={}", patients.size());
        return patients;
    }

    @Transactional(readOnly = true)
    public Patient findById(Long id) {
        LOG.debug("[FIND BY ID] Fetching Patient id={}", id);
        return patientRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Patient not found with id={}", id);
                    return new NotFoundAlertException(
                            "Patient not found with id " + id,
                            "patient",
                            "notfound"
                    );
                });
    }

    @Transactional(readOnly = true)
    public Page<Patient> findByAnyDocumentNumber(String numberPart, Pageable pageable) {
        LOG.debug("[FIND BY ANY DOCUMENT] numberPart='{}' pageable={}", numberPart, pageable);
        Page<PatientDocument> docsPage =
                patientDocumentRepository.findByNumberContainingIgnoreCase(numberPart, pageable);
        return docsPage.map(PatientDocument::getPatient);
    }

    private void handleConstraintsOnCreateOrUpdate(RuntimeException exception) {
        Throwable root = getRootCause(exception);
        String message = (root != null ? root.getMessage() : exception.getMessage());

        LOG.error("DB ROOT CAUSE: {}", message, exception);

        String lower = (message != null ? message.toLowerCase() : "");

        if (lower.contains("medical_record_number") || lower.contains("medicalrecordnumber")) {
            if (lower.contains("unique") || lower.contains("duplicate") || lower.contains("already exists")
                    || lower.contains("duplicate key") || lower.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "A patient with the same medicalRecordNumber already exists.",
                        "patient",
                        "unique.medical_record_number"
                );
            }
        }

        if (lower.contains("chk_patients_required_fields_when_not_unknown")
                || (lower.contains("check constraint") && lower.contains("unknown"))) {
            throw new BadRequestAlertException(
                    "Required fields are missing for a non-unknown patient.",
                    "patient",
                    "required.fields"
            );
        }

        if (lower.contains("medical_record_number") && (lower.contains("null value") || lower.contains("not-null"))) {
            throw new BadRequestAlertException(
                    "medicalRecordNumber was not generated by the database (check entity mapping to allow DB default).",
                    "patient",
                    "mrn.not.generated"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while saving patient (check required fields or unique constraints).",
                "patient",
                "db.constraint"
        );
    }

    private Specification<Patient> buildDuplicationSpec(
            Map<String, Boolean> fields,
            PatientDuplicationLookupDTO duplicationLookupDTO
    ) {
        return (patientRoot, criteriaQuery, criteriaBuilder) -> {

            LOG.debug("=== [DUPLICATION SPEC BUILD START] ===");
            LOG.debug("Incoming DTO => ruleId={}, firstName={}, lastName={}, gender={}, dob={}, documentNo={}",
                    duplicationLookupDTO.ruleId(),
                    duplicationLookupDTO.firstName(),
                    duplicationLookupDTO.lastName(),
                    duplicationLookupDTO.gender(),
                    duplicationLookupDTO.dateOfBirth(),
                    duplicationLookupDTO.documentNo()
            );
            LOG.debug("Active Rule Fields => {}", fields);

            List<Predicate> preds = new ArrayList<>();

            if (Boolean.TRUE.equals(fields.get("DOB"))) {
                LOG.debug("Checking DOB field...");
                if (duplicationLookupDTO.dateOfBirth() == null) {
                    LOG.debug("DOB is required by rule but DTO has null → returning disjunction");
                    return criteriaBuilder.disjunction();
                }
                LOG.debug("Comparing dateOfBirth DB column with value={}", duplicationLookupDTO.dateOfBirth());
                preds.add(criteriaBuilder.equal(patientRoot.get("dateOfBirth"), duplicationLookupDTO.dateOfBirth()));
            }

            if (Boolean.TRUE.equals(fields.get("GENDER"))) {
                LOG.debug("Checking GENDER field...");
                if (duplicationLookupDTO.gender() == null || duplicationLookupDTO.gender().isBlank()) {
                    LOG.debug("GENDER is required by rule but DTO has blank/null → returning disjunction");
                    return criteriaBuilder.disjunction();
                }
                LOG.debug("Comparing sexAtBirth with value={}", duplicationLookupDTO.gender().trim());
                preds.add(criteriaBuilder.equal(patientRoot.get("sexAtBirth"), duplicationLookupDTO.gender().trim()));
            }

            if (Boolean.TRUE.equals(fields.get("FIRST_NAME"))) {
                LOG.debug("Checking FIRST_NAME field...");
                if (duplicationLookupDTO.firstName() == null || duplicationLookupDTO.firstName().isBlank()) {
                    LOG.debug("FIRST_NAME is required but DTO empty → returning disjunction");
                    return criteriaBuilder.disjunction();
                }
                LOG.debug("Comparing firstName (lowercase) with value={}", duplicationLookupDTO.firstName().trim().toLowerCase());
                preds.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(patientRoot.get("firstName")),
                        duplicationLookupDTO.firstName().trim().toLowerCase()
                ));
            }

            if (Boolean.TRUE.equals(fields.get("LAST_NAME"))) {
                LOG.debug("Checking LAST_NAME field...");
                if (duplicationLookupDTO.lastName() == null || duplicationLookupDTO.lastName().isBlank()) {
                    LOG.debug("LAST_NAME required but DTO empty → returning disjunction");
                    return criteriaBuilder.disjunction();
                }
                LOG.debug("Comparing lastName (lowercase) with value={}", duplicationLookupDTO.lastName().trim().toLowerCase());
                preds.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(patientRoot.get("lastName")),
                        duplicationLookupDTO.lastName().trim().toLowerCase()
                ));
            }

            if (Boolean.TRUE.equals(fields.get("DOCUMENT_NO"))) {
                LOG.debug("Checking DOCUMENT_NO field...");
                if (duplicationLookupDTO.documentNo() == null || duplicationLookupDTO.documentNo().isBlank()) {
                    LOG.debug("DOCUMENT_NO required but DTO empty → returning disjunction");
                    return criteriaBuilder.disjunction();
                }
                LOG.debug("Comparing primaryDocumentNumber with value={}", duplicationLookupDTO.documentNo().trim());
                preds.add(criteriaBuilder.equal(
                        patientRoot.get("primaryDocumentNumber"),
                        duplicationLookupDTO.documentNo().trim()
                ));
            }

            LOG.debug("Total predicates added: {}", preds.size());
            LOG.debug("=== [DUPLICATION SPEC BUILD END] ===");

            return criteriaBuilder.and(preds.toArray(new Predicate[0]));
        };
    }

    public Page<Patient> findDuplicationCandidates(PatientDuplicationLookupDTO duplicationLookupDTO, Pageable pageable) {
        if (duplicationLookupDTO == null || duplicationLookupDTO.ruleId() == null) {
            return Page.empty(pageable);
        }

        DuplicationCandidate duplicationCandidate = duplicationCandidateRepository
                .findByIdAndIsActiveTrue(duplicationLookupDTO.ruleId())
                .orElse(null);

        if (duplicationCandidate == null || duplicationCandidate.getFields() == null || duplicationCandidate.getFields().isEmpty()) {
            return Page.empty(pageable);
        }

        Specification<Patient> spec = buildDuplicationSpec(duplicationCandidate.getFields(), duplicationLookupDTO);
        return patientRepository.findAll(spec, pageable);
    }

    private String safe(String value) {
        return value == null ? "" : value;
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

        /* ===================== 2. Document ===================== */

        PatientDocument document = patientDocumentRepository
                .findFirstByPatientIdAndIsPrimaryTrue(patientId)
                .orElse(null);

        String documentType = document != null ? document.getType().name() : null;
        String documentNumber = document != null ? document.getNumber() : null;

        /* ===================== 3. Address ===================== */

        /* ===================== 3. Address ===================== */

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
        /* ===================== 4. Insurance ===================== */

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

        /* ===================== 2. Document ===================== */

        PatientDocument document = patientDocumentRepository
                .findFirstByPatientIdAndIsPrimaryTrue(patientId)
                .orElse(null);

        String documentType = document != null ? document.getType().name() : null;
        String documentNumber = document != null ? document.getNumber() : null;

        /* ===================== 3. Address ===================== */

        /* ===================== 3. Address ===================== */

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
        /* ===================== 4. Insurance ===================== */

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