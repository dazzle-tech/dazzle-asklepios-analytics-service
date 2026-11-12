package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientProcedure;
import com.dazzle.asklepios.domain.PatientProcedurePerformance;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.PatientProcedurePerformanceRepository;
import com.dazzle.asklepios.repository.PatientProcedureRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportVerificationDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Answers the public "is this procedure report genuine?" check behind the report QR code.
 * The signature is checked before any database read, so unsigned ids reveal nothing.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProcedureReportVerificationService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureReportVerificationService.class);

    private final ReportVerificationService reportVerificationService;
    private final PatientProcedureRepository patientProcedureRepository;
    private final PatientProcedurePerformanceRepository performanceRepository;
    private final PatientRepository patientRepository;
    private final ProcedureRepository procedureRepository;
    private final FacilityRepository facilityRepository;
    private final DepartmentsRepository departmentsRepository;

    public ProcedureReportVerificationDTO verify(String token) {
        return reportVerificationService.verify(token)
                .map(v -> lookup(v.procedureRequestId(), v.issuedAt()))
                .orElseGet(() -> {
                    LOG.info("[ProcedureReportVerification] rejected token");
                    return ProcedureReportVerificationDTO.invalid();
                });
    }

    private ProcedureReportVerificationDTO lookup(Long id, Instant issuedAt) {
        Optional<PatientProcedure> found = patientProcedureRepository.findById(id);
        if (found.isEmpty()) {
            LOG.info("[ProcedureReportVerification] signed token for missing procedure id={}", id);
            return ProcedureReportVerificationDTO.invalid();
        }
        PatientProcedure proc = found.get();

        Patient patient = proc.getPatient() == null ? null
                : patientRepository.findById(proc.getPatient().getId()).orElse(null);
        String procedureName = proc.getProcedure() == null ? null
                : procedureRepository.findById(proc.getProcedure().getId()).map(p -> p.getName()).orElse(null);
        PatientProcedurePerformance perf = performanceRepository.findFirstByPatientProcedureIdOrderByIdDesc(id).orElse(null);
        Instant performed = perf == null ? null
                : Objects.requireNonNullElse(perf.getActualEndTime(), perf.getActualStartTime());

        return new ProcedureReportVerificationDTO(
                true,
                "PRC-" + String.format(Locale.ROOT, "%06d", id),
                facilityName(proc),
                procedureName,
                proc.getStatus(),
                patient == null ? null
                        : initials(patient.getFirstName(), patient.getSecondName(), patient.getThirdName(), patient.getLastName()),
                patient == null ? null : maskMrn(patient.getMedicalRecordNumber()),
                performed,
                issuedAt
        );
    }

    /** The performing facility, else the requesting department's facility. */
    private String facilityName(PatientProcedure proc) {
        if (proc.getToFacilityId() != null) {
            Optional<String> name = facilityRepository.findById(proc.getToFacilityId()).map(f -> f.getName());
            if (name.isPresent()) {
                return name.get();
            }
        }
        if (proc.getFromDepartmentId() == null) {
            return null;
        }
        return departmentsRepository.findById(proc.getFromDepartmentId())
                .map(Department::getFacility)
                .map(f -> f.getName())
                .orElse(null);
    }

    /** "masa nabeel zahran" becomes "M. N. Z." */
    static String initials(String... names) {
        String value = Stream.of(names)
                .filter(n -> n != null && !n.isBlank())
                .map(n -> n.trim().substring(0, 1).toUpperCase(Locale.ROOT) + ".")
                .collect(Collectors.joining(" "));
        return value.isEmpty() ? null : value;
    }

    /** Keeps the first two and the last character: "P14738" becomes "P1***8". */
    static String maskMrn(String mrn) {
        if (mrn == null || mrn.isBlank()) {
            return null;
        }
        String v = mrn.trim();
        if (v.length() <= 3) {
            return v.charAt(0) + "*".repeat(v.length() - 1);
        }
        return v.substring(0, 2) + "*".repeat(v.length() - 3) + v.charAt(v.length() - 1);
    }
}
