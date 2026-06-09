package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestReport;
import com.dazzle.asklepios.domain.DiagnosticOrderTestResult;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestResultRepository;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.service.dto.LaboratoryResultReportDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
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
public class DiagnosticOrderTestResultReportService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderTestResultReportService.class);

    private final DiagnosticOrderTestResultRepository diagnosticOrderTestResultRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticOrderRepository orderRepository;
    private final PatientRepository patientRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final DepartmentsRepository departmentRepository;
    private final PatientEncounterRepository encounterRepository;
    private final DiagnosticTestProfileRepository diagnosticTestProfileRepository;
    private final ApLovValueRepository apLovValueRepository;
    private final ReportCommonService reportCommonService;
    public DiagnosticOrderTestResultReportService(
            DiagnosticOrderTestResultRepository diagnosticOrderTestResultRepository,
            DiagnosticOrderTestRepository diagnosticOrderTestRepository, DiagnosticOrderRepository orderRepository, PatientRepository patientRepository, DiagnosticTestRepository diagnosticTestRepository, DepartmentsRepository departmentRepository, PatientEncounterRepository encounterRepository, DiagnosticTestProfileRepository diagnosticTestProfileRepository, ApLovValueRepository apLovValueRepository, ReportCommonService reportCommonService
    ) {
        this.diagnosticOrderTestResultRepository = diagnosticOrderTestResultRepository;
        this.diagnosticOrderTestRepository = diagnosticOrderTestRepository;
        this.orderRepository = orderRepository;
        this.patientRepository = patientRepository;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.departmentRepository = departmentRepository;
        this.encounterRepository = encounterRepository;
        this.diagnosticTestProfileRepository = diagnosticTestProfileRepository;
        this.apLovValueRepository = apLovValueRepository;
        this.reportCommonService = reportCommonService;
    }


    public LaboratoryResultReportDTO getLaboratoryResult(Long diagnosticTestResultId) {

        LOG.debug("[LaboratoryResultReportService] GET_LABORATORY_RESULT_REPORT - start. reportId={}", diagnosticTestResultId);

        DiagnosticOrderTestResult result = diagnosticOrderTestResultRepository.findById(diagnosticTestResultId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_order_test_result",
                        "DiagnosticOrderTestResult not found with id " + diagnosticTestResultId
                ));

        DiagnosticOrderTest orderTest = diagnosticOrderTestRepository.findById(result.getOrderTestId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_order_tests",
                        "DiagnosticOrderTest not found with id " + result.getOrderTestId()
                ));

        Long orderId = orderTest.getOrderId();
        if (orderId == null) {
            throw new BadRequestAlertException(
                    "invalid_order",
                    "diagnostic_order_tests",
                    "OrderId is null for orderTestId " + result.getOrderTestId()
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
        DiagnosticTestProfile testProfile = diagnosticTestProfileRepository.findById(result.getProfileTestId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_tests_profile",
                        "Diagnostic test profile not found with id " + result.getProfileTestId()
                ));



        String patientName = (patient.getFirstName() + " " + patient.getLastName()).trim();
        String mrn = patient.getMedicalRecordNumber();
        String facilityName = reportCommonService.getFacilityNameFromDepartment(orderTest.getReceivedDepartmentId());
        String departmentName = reportCommonService.getDepartmentName(orderTest.getReceivedDepartmentId());
        String fromDepartmentName = reportCommonService.getDepartmentName(order.getFromDepartmentId());
        String age = reportCommonService.calculateAge(patient.getDateOfBirth());
        LOG.debug(
                "[LaboratoryResultReportService] GET_LABORATORY_RESULT_REPORT - data prepared. orderTestId={} patient={} test={}",
                orderTest.getId(),
                patientName,
                test.getName()
        );
        String normalRangeValue =
                testProfile.getResultType() == TestResultType.LOV
                        ? apLovValueRepository.findById(String.valueOf(result.getNormalRangeValue()))
                        .map(ApLovValue::getLovDisplayVale)
                        .orElse(result.getNormalRangeValue())
                        : result.getNormalRangeValue();

        String resultValue =
                testProfile.getResultType() == TestResultType.LOV
                        ? apLovValueRepository.findById(String.valueOf(result.getResultValueText()))
                        .map(ApLovValue::getLovDisplayVale)
                        .orElse(result.getResultValueText())
                        : (result.getResultValueNumber() != null
                        ? result.getResultValueNumber().toString()
                        : null);

        return new LaboratoryResultReportDTO(
                facilityName,
                departmentName,

                patientName,
                mrn,
                patient.getDateOfBirth(),
                age,
                patient.getSexAtBirth(),
                patient.getPrimaryMobileNumber(),

                encounter.getEncounterNumber(),
                order.getOrderNumber(),
                result.getCreatedDate(),
                normalRangeValue,
                fromDepartmentName,
                testProfile.getName(),

                resultValue,
                reportCommonService.getLovDisplayValue(String.valueOf(testProfile.getResultUnit())),
                result.getMarker(),
                result.getReviewDate(),
                reportCommonService.getDisplayUserName( result.getReviewBy())

        );
    }
}
