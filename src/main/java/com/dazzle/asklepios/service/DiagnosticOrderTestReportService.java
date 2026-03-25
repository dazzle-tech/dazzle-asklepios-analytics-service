package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestReport;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestReportRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.service.dto.RadiologyReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

/**
 * Service layer for managing {@link DiagnosticOrderTestReport}.
 *
 * <p>This service owns all validations and persistence access for radiology reports:
 * test constraints (existence/type/order match), prerequisites (ACCEPTED), uniqueness,
 * and image workflow requirements.</p>
 */
@Service
@Transactional
public class DiagnosticOrderTestReportService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderTestReportService.class);

    private final DiagnosticOrderTestReportRepository diagnosticOrderTestReportRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticOrderRepository orderRepository;
    private final PatientRepository patientRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final DepartmentsRepository departmentRepository;
    private final PatientEncounterRepository encounterRepository;

    public DiagnosticOrderTestReportService(
            DiagnosticOrderTestReportRepository diagnosticOrderTestReportRepository,
            DiagnosticOrderTestRepository diagnosticOrderTestRepository, DiagnosticOrderRepository orderRepository, PatientRepository patientRepository, DiagnosticTestRepository diagnosticTestRepository, DepartmentsRepository departmentRepository, PatientEncounterRepository encounterRepository
    ) {
        this.diagnosticOrderTestReportRepository = diagnosticOrderTestReportRepository;
        this.diagnosticOrderTestRepository = diagnosticOrderTestRepository;
        this.orderRepository = orderRepository;
        this.patientRepository = patientRepository;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.departmentRepository = departmentRepository;
        this.encounterRepository = encounterRepository;
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

    public RadiologyReportDTO getRadiologyReport(Long diagnosticTestReportId) {

        LOG.debug("[RadiologyReportService] GET_RADIOLOGY_REPORT - start. reportId={}", diagnosticTestReportId);

        DiagnosticOrderTestReport report = diagnosticOrderTestReportRepository.findById(diagnosticTestReportId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_order_test_report",
                        "DiagnosticOrderTestReport not found with id " + diagnosticTestReportId
                ));

        DiagnosticOrderTest orderTest = diagnosticOrderTestRepository.findById(report.getOrderTestId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_order_tests",
                        "DiagnosticOrderTest not found with id " + report.getOrderTestId()
                ));

        Long orderId = orderTest.getOrderId();
        if (orderId == null) {
            throw new BadRequestAlertException(
                    "invalid_order",
                    "diagnostic_order_tests",
                    "OrderId is null for orderTestId " + report.getOrderTestId()
            );
        }

        DiagnosticOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_orders",
                        "Order not found with id " + orderId
                ));

        Patient patient = patientRepository.findById(order.getPatientId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "patients",
                        "Patient not found with id " + order.getPatientId()
                ));

        PatientEncounter encounter = encounterRepository.findById(order.getEncounterId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "patientEncounters",
                        "Encounter not found with id " + order.getEncounterId()
                ));
        DiagnosticTest test = diagnosticTestRepository.findById(orderTest.getTestId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_tests",
                        "Diagnostic test not found with id " + orderTest.getTestId()
                ));

        Department department = departmentRepository.findById(orderTest.getReceivedDepartmentId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "departments",
                        "Department not found with id " + orderTest.getReceivedDepartmentId()
                ));
        Department fromDepartment = departmentRepository.findById(order.getFromDepartmentId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "departments",
                        "Department not found with id " + order.getFromDepartmentId()
                ));

        String patientName = (patient.getFirstName() + " " + patient.getLastName()).trim();
        String mrn = patient.getMedicalRecordNumber();
        String facilityName = department.getFacility().getName();
        String departmentName = department.getName();
        String fromDepartmentName = fromDepartment.getName();
        String age = calculateAge(patient.getDateOfBirth());
        LOG.debug(
                "[RadiologyReportService] GET_RADIOLOGY_REPORT - data prepared. orderTestId={} patient={} test={}",
                orderTest.getId(),
                patientName,
                test.getName()
        );

        return new RadiologyReportDTO(
                facilityName,
                departmentName,

                patientName,
                mrn,
                patient.getDateOfBirth(),
                age,
                patient.getSexAtBirth(),
                patient.getPrimaryMobileNumber(),

                encounter.getEncounterNumber(),
                order.getCreatedBy(),
                fromDepartmentName,
                test.getName(),

                report.getReport(),
                report.getSeverity(),
                report.getApprovedBy(),
                report.getReviewBy()
        );
    }
}
