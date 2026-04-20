package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientAllergies;
import com.dazzle.asklepios.domain.PatientDiagnosis;
import com.dazzle.asklepios.domain.PatientDocument;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.PatientInsurance;
import com.dazzle.asklepios.domain.PatientPrescription;
import com.dazzle.asklepios.domain.PatientPrescriptionMedication;
import com.dazzle.asklepios.domain.PatientWarnings;
import com.dazzle.asklepios.domain.PrescriptionInstruction;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiagnosisRepository;
import com.dazzle.asklepios.repository.PatientAllergyRepository;
import com.dazzle.asklepios.repository.PatientDocumentRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientInsuranceRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.PatientWarningRepository;
import com.dazzle.asklepios.repository.PrescriptionInstructionRepository;
import com.dazzle.asklepios.repository.PrescriptionMedicationRepository;
import com.dazzle.asklepios.repository.PrescriptionRepository;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionAllergyDTO;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionDiagnosisDTO;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionMedicationDTO;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionPrintDTO;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionWarningDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PrescriptionReportService {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionReportService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMedicationRepository prescriptionMedicationRepository;
    private final PatientRepository patientRepository;
    private final PatientEncounterRepository encounterRepository;
    private final DepartmentsRepository departmentRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final PatientAllergyRepository patientAllergyRepository;
    private final PatientWarningRepository patientWarningRepository;
    private final PatientInsuranceRepository patientInsuranceRepository;
    private final PatientDocumentRepository patientDocumentRepository;
    private final ApLovValueRepository apLovValueRepository;
    private final PrescriptionInstructionRepository prescriptionInstructionRepository;

    public PrescriptionReportService(
            PrescriptionRepository prescriptionRepository,
            PrescriptionMedicationRepository prescriptionMedicationRepository,
            PatientRepository patientRepository,
            PatientEncounterRepository encounterRepository,
            DepartmentsRepository departmentRepository,
            DiagnosisRepository diagnosisRepository,
            PatientAllergyRepository patientAllergyRepository,
            PatientWarningRepository patientWarningRepository,
            PatientInsuranceRepository patientInsuranceRepository,
            PatientDocumentRepository patientDocumentRepository,
            ApLovValueRepository apLovValueRepository,
            PrescriptionInstructionRepository prescriptionInstructionRepository
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionMedicationRepository = prescriptionMedicationRepository;
        this.patientRepository = patientRepository;
        this.encounterRepository = encounterRepository;
        this.departmentRepository = departmentRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.patientAllergyRepository = patientAllergyRepository;
        this.patientWarningRepository = patientWarningRepository;
        this.patientInsuranceRepository = patientInsuranceRepository;
        this.patientDocumentRepository = patientDocumentRepository;
        this.apLovValueRepository = apLovValueRepository;
        this.prescriptionInstructionRepository = prescriptionInstructionRepository;
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

    private String resolveLovDisplayValue(Object key) {
        if (key == null) {
            return null;
        }

        return apLovValueRepository.findById(String.valueOf(key))
                .map(ApLovValue::getLovDisplayVale)
                .orElse(null);
    }

    private String resolveLovDisplayValues(String commaSeparatedKeys) {
        if (commaSeparatedKeys == null || commaSeparatedKeys.isBlank()) {
            return null;
        }

        List<String> keys = Arrays.stream(commaSeparatedKeys.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (keys.isEmpty()) {
            return null;
        }

        Map<String, String> lovMap = apLovValueRepository.findByKeyIn(keys).stream()
                .collect(Collectors.toMap(
                        ApLovValue::getKey,
                        ApLovValue::getLovDisplayVale
                ));

        return Arrays.stream(commaSeparatedKeys.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(lovMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    public PrescriptionPrintDTO getPrescriptionPrint(Long prescriptionId) {
        LOG.debug("[PrescriptionPrintService] GET_PRESCRIPTION_PRINT - start. prescriptionId={}", prescriptionId);

        PatientPrescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "prescriptions",
                        "Prescription not found with id " + prescriptionId
                ));

        if (prescription.getPatient() == null || prescription.getPatient().getId() == null) {
            throw new BadRequestAlertException(
                    "invalid_patient",
                    "prescriptions",
                    "PatientId is null for prescriptionId " + prescriptionId
            );
        }

        if (prescription.getEncounterId() == null) {
            throw new BadRequestAlertException(
                    "invalid_encounter",
                    "prescriptions",
                    "EncounterId is null for prescriptionId " + prescriptionId
            );
        }

        if (prescription.getFromDepartmentId() == null) {
            throw new BadRequestAlertException(
                    "invalid_department",
                    "prescriptions",
                    "FromDepartmentId is null for prescriptionId " + prescriptionId
            );
        }

        Patient patient = patientRepository.findById(prescription.getPatient().getId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "patients",
                        "Patient not found with id " + prescription.getPatient().getId()
                ));

        PatientEncounter encounter = encounterRepository.findById(prescription.getEncounterId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "patientEncounters",
                        "Encounter not found with id " + prescription.getEncounterId()
                ));

        Department fromDepartment = departmentRepository.findById(prescription.getFromDepartmentId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "departments",
                        "Department not found with id " + prescription.getFromDepartmentId()
                ));

        List<PatientPrescriptionMedication> medications =
                Optional.ofNullable(prescriptionMedicationRepository.findAllByPrescriptionHeaderIdOrderByIdAsc(prescriptionId))
                        .orElse(Collections.emptyList());

        List<PatientDiagnosis> diagnoses =
                Optional.ofNullable(diagnosisRepository.findAllByEncounterId(encounter.getId()))
                        .orElse(Collections.emptyList());

        List<PatientAllergies> allergies =
                Optional.ofNullable(patientAllergyRepository.findAllByPatientId(patient.getId()))
                        .orElse(Collections.emptyList());

        List<PatientWarnings> warnings =
                Optional.ofNullable(patientWarningRepository.findAllByPatientId(patient.getId()))
                        .orElse(Collections.emptyList());

        PatientInsurance insuranceEntity = patientInsuranceRepository
                .findTopByPatient_IdOrderByIdDesc(patient.getId());

        String insuranceName = insuranceEntity != null && insuranceEntity.getPayor() != null
                ? insuranceEntity.getPayor().getName()
                : null;

        String patientName = ((patient.getFirstName() == null ? "" : patient.getFirstName()) + " " +
                (patient.getLastName() == null ? "" : patient.getLastName())).trim();

        String age = calculateAge(patient.getDateOfBirth());

        String facilityName = fromDepartment.getFacility() != null
                ? fromDepartment.getFacility().getName()
                : null;

        PatientDocument patientDocument = patientDocumentRepository
                .findTopByPatientIdAndIsPrimaryTrueOrderByIdDesc(patient.getId())
                .orElse(null);

        String primaryDocumentType = patientDocument != null && patientDocument.getType() != null
                ? patientDocument.getType().name()
                : null;

        String primaryDocumentNumber = patientDocument != null
                ? patientDocument.getNumber()
                : null;

        List<PrescriptionDiagnosisDTO> diagnosisDTOS = diagnoses.stream()
                .map(d -> new PrescriptionDiagnosisDTO(
                        d.getDiagnosis() != null ? d.getDiagnosis().getIcdShortDescription() : null,
                        d.getCreatedDate()
                ))
                .toList();

        List<PrescriptionAllergyDTO> allergyDTOS = allergies.stream()
                .map(a -> new PrescriptionAllergyDTO(
                        a.getAllergenType(),
                        a.getAllergen() != null ? a.getAllergen().getName() : null,
                        a.getSeverity() != null ? a.getSeverity().name() : null
                ))
                .toList();

        List<PrescriptionWarningDTO> warningDTOS = warnings.stream()
                .map(w -> new PrescriptionWarningDTO(
                        resolveLovDisplayValue(w.getWarningType()),
                        w.getWarning(),
                        w.getSeverity() != null ? w.getSeverity().name() : null
                ))
                .toList();

        List<PrescriptionMedicationDTO> medicationDTOS = medications.stream()
                .sorted(Comparator.comparing(PatientPrescriptionMedication::getId))
                .map(m -> new PrescriptionMedicationDTO(
                        m.getMedications() != null ? m.getMedications().getName() : null,
                        resolveInstruction(m),
                        m.getDuration(),
                        m.getNumberOfRefills() != null && m.getNumberOfRefills() > 0,
                        m.getNumberOfRefills(),
                        resolveLovDisplayValues(m.getAdministrationInstructions()),
                        m.getAllowedSubstitute(),
                        m.getIndicationIcd() != null ? m.getIndicationIcd().getIcdShortDescription() : null
                ))
                .toList();

        LOG.debug(
                "[PrescriptionPrintService] GET_PRESCRIPTION_PRINT - data prepared. prescriptionId={} patient={} medications={}",
                prescriptionId,
                patientName,
                medicationDTOS.size()
        );

        return new PrescriptionPrintDTO(
                prescription.getPrescriptionNum(),
                prescription.getCreatedDate(),
                prescription.getUrgencyLevel() != null ? prescription.getUrgencyLevel().name() : null,
                prescription.getCreatedBy(),
                facilityName,
                fromDepartment.getName(),

                patientName,
                patient.getMedicalRecordNumber(),
                primaryDocumentType,
                primaryDocumentNumber,
                patient.getDateOfBirth(),
                age,
                patient.getSexAtBirth(),
                patient.getPrimaryMobileNumber(),
                patient.getEmail(),
                insuranceName,

                encounter.getEncounterNumber(),
                encounter.getEncounterDate(),
                encounter.getEncounterReason(),
                fromDepartment.getName(),

                diagnosisDTOS,
                allergyDTOS,
                warningDTOS,
                medicationDTOS
        );
    }

    private String resolveInstruction(PatientPrescriptionMedication medication) {
        if (medication == null || medication.getInstructionsType() == null) {
            LOG.debug("[resolveInstruction] medication is null or instructionsType is null");
            return null;
        }

        LOG.debug("[resolveInstruction] START medicationId={} instructionsType={} rawInstructions={}",
                medication.getId(),
                medication.getInstructionsType(),
                medication.getInstructions());

        return switch (medication.getInstructionsType()) {

            case MANUAL_INSTRUCTIONS -> {
                LOG.debug("[resolveInstruction] MANUAL_INSTRUCTIONS medicationId={} result={}",
                        medication.getId(),
                        medication.getInstructions());
                yield medication.getInstructions();
            }

            case CUSTOM_INSTRUCTIONS -> {
                LOG.debug("[resolveInstruction] CUSTOM_INSTRUCTIONS medicationId={}", medication.getId());
                String result = buildCustomInstruction(medication);
                LOG.debug("[resolveInstruction] CUSTOM_INSTRUCTIONS medicationId={} result={}",
                        medication.getId(),
                        result);
                yield result;
            }

            case PRE_DEFINED_INSTRUCTIONS -> {
                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} rawInstructions={}",
                        medication.getId(),
                        medication.getInstructions());

                Long id = safeParse(medication.getInstructions());

                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} parsedId={}",
                        medication.getId(),
                        id);

                if (id == null) {
                    LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} parsedId is null",
                            medication.getId());
                    yield null;
                }

                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} calling repository.findById({})",
                        medication.getId(),
                        id);

                Optional<PrescriptionInstruction> instructionOptional = prescriptionInstructionRepository.findById(id);

                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} repository result present={}",
                        medication.getId(),
                        instructionOptional.isPresent());

                if (instructionOptional.isEmpty()) {
                    yield null;
                }

                PrescriptionInstruction instruction = instructionOptional.get();

                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} dbInstruction dose={} unit={} rout={} frequency={}",
                        medication.getId(),
                        instruction.getDose(),
                        instruction.getUnit(),
                        instruction.getRout(),
                        instruction.getFrequency());

                String result = buildPredefinedInstruction(instruction);

                LOG.debug("[resolveInstruction] PRE_DEFINED_INSTRUCTIONS medicationId={} result={}",
                        medication.getId(),
                        result);

                yield result;
            }
        };
    }

    private String buildCustomInstruction(PatientPrescriptionMedication medication) {
        LOG.debug("[buildCustomInstruction] START medicationId={}", medication.getId());

        List<String> parts = new ArrayList<>();

        if (medication.getDose() != null) {
            LOG.debug("[buildCustomInstruction] medicationId={} dose={}", medication.getId(), medication.getDose());

            String dosePart = medication.getDose().toString();

            if (medication.getDoesUnit() != null && !medication.getDoesUnit().isBlank()) {
                LOG.debug("[buildCustomInstruction] medicationId={} doseUnitKey={}",
                        medication.getId(),
                        medication.getDoesUnit());

                String unitDisplay = apLovValueRepository.findById(String.valueOf(medication.getDoesUnit()))
                        .map(ApLovValue::getLovDisplayVale)
                        .orElse(null);

                LOG.debug("[buildCustomInstruction] medicationId={} doseUnitDisplay={}",
                        medication.getId(),
                        unitDisplay);

                dosePart += " " + unitDisplay;
            }

            parts.add(dosePart);
        }

        if (medication.getRout() != null && !medication.getRout().isBlank()) {
            LOG.debug("[buildCustomInstruction] medicationId={} rout={}",
                    medication.getId(),
                    medication.getRout());
            parts.add(medication.getRout());
        }

        if (medication.getFrequency() != null && !medication.getFrequency().isBlank()) {
            LOG.debug("[buildCustomInstruction] medicationId={} frequencyKey={}",
                    medication.getId(),
                    medication.getFrequency());

            String frequencyDisplay = apLovValueRepository.findById(String.valueOf(medication.getFrequency()))
                    .map(ApLovValue::getLovDisplayVale)
                    .orElse(null);

            LOG.debug("[buildCustomInstruction] medicationId={} frequencyDisplay={}",
                    medication.getId(),
                    frequencyDisplay);

            parts.add(frequencyDisplay);
        }

        String result = parts.isEmpty() ? null : String.join(" - ", parts);

        LOG.debug("[buildCustomInstruction] END medicationId={} result={}",
                medication.getId(),
                result);

        return result;
    }

    private String buildPredefinedInstruction(PrescriptionInstruction instruction) {
        LOG.debug("[buildPredefinedInstruction] START instructionId={}",
                instruction != null ? instruction.getId() : null);

        if (instruction == null) {
            LOG.debug("[buildPredefinedInstruction] instruction is null");
            return null;
        }

        LOG.debug("[buildPredefinedInstruction] instructionId={} dose={} unit={} rout={} frequency={}",
                instruction.getId(),
                instruction.getDose(),
                instruction.getUnit(),
                instruction.getRout(),
                instruction.getFrequency());

        StringBuilder sb = new StringBuilder();

        if (instruction.getDose() != null) {
            sb.append(instruction.getDose());
        }

        if (instruction.getUnit() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(instruction.getUnit());
        }

        if (instruction.getRout() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(instruction.getRout());
        }

        if (instruction.getFrequency() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(instruction.getFrequency());
        }

        String result = sb.isEmpty() ? null : sb.toString();

        LOG.debug("[buildPredefinedInstruction] END instructionId={} result={}",
                instruction.getId(),
                result);

        return result;
    }

    private Long safeParse(String value) {
        LOG.debug("[safeParse] rawValue={}", value);
        try {
            Long parsed = value != null ? Long.parseLong(value) : null;
            LOG.debug("[safeParse] parsedValue={}", parsed);
            return parsed;
        } catch (NumberFormatException e) {
            LOG.debug("[safeParse] failed to parse value={}", value, e);
            return null;
        }
    }
}