package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestReport;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestReportRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.service.dto.RadiologyReportDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing {@link DiagnosticOrderTestReport}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DiagnosticOrderTestReportService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderTestReportService.class);

    private final DiagnosticOrderTestReportRepository diagnosticOrderTestReportRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticOrderRepository orderRepository;
    private final PatientRepository patientRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final PatientEncounterRepository encounterRepository;
    private final ReportCommonService reportCommonService;

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

        String patientName = reportCommonService.getPatientDisplayName(patient);
        String facilityName = reportCommonService.getFacilityNameFromDepartment(orderTest.getReceivedDepartmentId());
        String departmentName = reportCommonService.getDepartmentName(orderTest.getReceivedDepartmentId());
        String fromDepartmentName = reportCommonService.getDepartmentName(order.getFromDepartmentId());
        String age = reportCommonService.calculateAge(patient.getDateOfBirth());

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
                patient.getMedicalRecordNumber(),
                patient.getDateOfBirth(),
                age,
                patient.getSexAtBirth(),
                patient.getPrimaryMobileNumber(),

                encounter.getEncounterNumber(),
                reportCommonService.getDisplayUserName(order.getCreatedBy()),
                fromDepartmentName,
                test.getName(),

                report.getReport(),
                reportCommonService.formatEnum(report.getSeverity()),
                report.getCriticalFindings(),
                report.getRadiologistComments(),
                report.getRadiologistInformation(),
                reportCommonService.getDisplayUserName(report.getApprovedBy()),
                reportCommonService.getDisplayUserName(report.getReviewBy())
        );
    }
}