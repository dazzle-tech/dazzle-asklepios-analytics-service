package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Address;
import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientDocument;
import com.dazzle.asklepios.domain.PatientInsurance;
import com.dazzle.asklepios.domain.PatientPreferredHealthProfessional;
import com.dazzle.asklepios.repository.AddressRepository;
import com.dazzle.asklepios.repository.DuplicationCandidateRepository;
import com.dazzle.asklepios.repository.PatientDocumentRepository;
import com.dazzle.asklepios.repository.PatientInsuranceRepository;
import com.dazzle.asklepios.repository.PatientPreferredHealthProfessionalRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.service.dto.patient.PatientCreateDTO;
import com.dazzle.asklepios.service.dto.patient.PatientDuplicationLookupDTO;
import com.dazzle.asklepios.service.dto.patient.PatientInformationReportDTO;
import com.dazzle.asklepios.service.dto.patient.PatientLabelDTO;
import com.dazzle.asklepios.service.dto.patient.PatientUpdateDTO;
import com.dazzle.asklepios.service.dto.patient.UnknownPatientCreateDTO;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class PatientService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final PatientDocumentRepository patientDocumentRepository;
    private final DuplicationCandidateRepository duplicationCandidateRepository;
    private final AddressRepository addressRepository;
    private final PatientInsuranceRepository patientInsuranceRepository;
    private final PatientPreferredHealthProfessionalRepository patientPreferredHealthProfessionalRepository;

    public PatientService(
            PatientRepository patientRepository,
            PatientDocumentRepository patientDocumentRepository,
            DuplicationCandidateRepository duplicationCandidateRepository,
            AddressRepository addressRepository,
            PatientInsuranceRepository patientInsuranceRepository,
            PatientPreferredHealthProfessionalRepository patientPreferredHealthProfessionalRepository
    ) {
        this.patientRepository = patientRepository;
        this.patientDocumentRepository = patientDocumentRepository;
        this.duplicationCandidateRepository = duplicationCandidateRepository;
        this.addressRepository = addressRepository;
        this.patientInsuranceRepository = patientInsuranceRepository;
        this.patientPreferredHealthProfessionalRepository = patientPreferredHealthProfessionalRepository;
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
                if (duplicationLookupDTO.dateOfBirth() == null) {
                    return criteriaBuilder.disjunction();
                }
                preds.add(criteriaBuilder.equal(patientRoot.get("dateOfBirth"), duplicationLookupDTO.dateOfBirth()));
            }

            if (Boolean.TRUE.equals(fields.get("GENDER"))) {
                if (duplicationLookupDTO.gender() == null || duplicationLookupDTO.gender().isBlank()) {
                    return criteriaBuilder.disjunction();
                }
                preds.add(criteriaBuilder.equal(patientRoot.get("sexAtBirth"), duplicationLookupDTO.gender().trim()));
            }

            if (Boolean.TRUE.equals(fields.get("FIRST_NAME"))) {
                if (duplicationLookupDTO.firstName() == null || duplicationLookupDTO.firstName().isBlank()) {
                    return criteriaBuilder.disjunction();
                }
                preds.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(patientRoot.get("firstName")),
                        duplicationLookupDTO.firstName().trim().toLowerCase()
                ));
            }

            if (Boolean.TRUE.equals(fields.get("LAST_NAME"))) {
                if (duplicationLookupDTO.lastName() == null || duplicationLookupDTO.lastName().isBlank()) {
                    return criteriaBuilder.disjunction();
                }
                preds.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(patientRoot.get("lastName")),
                        duplicationLookupDTO.lastName().trim().toLowerCase()
                ));
            }

            if (Boolean.TRUE.equals(fields.get("DOCUMENT_NO"))) {
                if (duplicationLookupDTO.documentNo() == null || duplicationLookupDTO.documentNo().isBlank()) {
                    return criteriaBuilder.disjunction();
                }
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

    // TODO move this logic to analytic service
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

        PatientDocument document = patientDocumentRepository
                .findFirstByPatientIdAndIsPrimaryTrue(patientId)
                .orElse(null);

        String documentType = document != null ? document.getType().name() : null;
        String documentNumber = document != null ? document.getNumber() : null;

        Address address = addressRepository
                .findFirstByPatientIdAndIsCurrentTrue(patientId)
                .orElse(null);

        String street = null;
        String city = null;
        String country = null;

        if (address != null) {
            street = address.getStreetName();
            if (address.getLocationJson() != null) {
                if (address.getLocationJson().getArea() != null)
                    city = address.getLocationJson().getArea().getName();
                if (address.getLocationJson().getCountry() != null)
                    country = address.getLocationJson().getCountry().getName();
            }
        }

        PatientInsurance insurance = patientInsuranceRepository
                .findFirstByPatientIdAndIsPrimaryTrue(patientId)
                .orElse(null);

        String insuranceProvider = insurance != null ? String.valueOf(insurance.getPayorId()) : null;
        String policyNumber = insurance != null ? String.valueOf(insurance.getPolicyNumber()) : null;

        PatientPreferredHealthProfessional preferred =
                patientPreferredHealthProfessionalRepository
                        .findFirstByPatientId(patientId)
                        .orElse(null);

        String preferredDoctor = preferred != null ? String.valueOf(preferred.getPractitionerId()) : null;

        return new PatientInformationReportDTO(
                patient.getId(),
                fullName,
                patient.getMedicalRecordNumber(),
                patient.getDateOfBirth().toInstant(),
                age,
                patient.getSexAtBirth() != null ? patient.getSexAtBirth().name() : null,
                null,
                documentType,
                documentNumber,
                patient.getPrimaryMobileNumber(),
                patient.getSecondMobileNumber(),
                patient.getEmail(),
                street,
                city,
                null,
                country,
                patient.getEmergencyContactName(),
                patient.getEmergencyContactRelation(),
                patient.getEmergencyContactPhone(),
                patient.getCreatedDate(),
                insuranceProvider,
                policyNumber,
                preferredDoctor
        );
    }
}