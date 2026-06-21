package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestReport;
import com.dazzle.asklepios.domain.DiagnosticOrderTestResult;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.PatientEncounter;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestResultRepository;
import com.dazzle.asklepios.repository.DiagnosticTestLaboratoryRepository;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientEncounterRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.service.dto.laboratory.LaboratoryOrderSectionDTO;
import com.dazzle.asklepios.service.dto.laboratory.LaboratoryOrderTestSectionDTO;
import com.dazzle.asklepios.service.dto.laboratory.LaboratoryResultItemDTO;
import com.dazzle.asklepios.service.dto.laboratory.LaboratoryResultReportDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final DiagnosticTestLaboratoryRepository diagnosticTestLaboratoryRepository;
    private final ApLovValueRepository apLovValueRepository;
    private final ReportCommonService reportCommonService;

    public DiagnosticOrderTestResultReportService(
            DiagnosticOrderTestResultRepository diagnosticOrderTestResultRepository,
            DiagnosticOrderTestRepository diagnosticOrderTestRepository,
            DiagnosticOrderRepository orderRepository,
            PatientRepository patientRepository,
            DiagnosticTestRepository diagnosticTestRepository,
            DepartmentsRepository departmentRepository,
            PatientEncounterRepository encounterRepository,
            DiagnosticTestProfileRepository diagnosticTestProfileRepository,
            DiagnosticTestLaboratoryRepository diagnosticTestLaboratoryRepository,
            ApLovValueRepository apLovValueRepository,
            ReportCommonService reportCommonService
    ) {
        this.diagnosticOrderTestResultRepository = diagnosticOrderTestResultRepository;
        this.diagnosticOrderTestRepository = diagnosticOrderTestRepository;
        this.orderRepository = orderRepository;
        this.patientRepository = patientRepository;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.departmentRepository = departmentRepository;
        this.encounterRepository = encounterRepository;
        this.diagnosticTestProfileRepository = diagnosticTestProfileRepository;
        this.diagnosticTestLaboratoryRepository = diagnosticTestLaboratoryRepository;
        this.apLovValueRepository = apLovValueRepository;
        this.reportCommonService = reportCommonService;
    }

    public LaboratoryResultReportDTO getLaboratoryResults(List<Long> resultIds) {

        LOG.debug("[LaboratoryResultReportService] GET_LABORATORY_RESULTS_REPORT - start. resultIds={}", resultIds);

        if (resultIds == null || resultIds.isEmpty()) {
            throw new BadRequestAlertException(
                    "invalid_results",
                    "diagnostic_order_test_result",
                    "Result ids are required"
            );
        }

        List<DiagnosticOrderTestResult> results =
                diagnosticOrderTestResultRepository.findAllById(resultIds);

        if (results.size() != resultIds.size()) {
            throw new BadRequestAlertException(
                    "notfound",
                    "diagnostic_order_test_result",
                    "Some DiagnosticOrderTestResult ids were not found"
            );
        }

        Map<Long, DiagnosticOrderTest> orderTestMap = results.stream()
                .map(DiagnosticOrderTestResult::getOrderTestId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> diagnosticOrderTestRepository.findById(id)
                                .orElseThrow(() -> new BadRequestAlertException(
                                        "notfound",
                                        "diagnostic_order_tests",
                                        "DiagnosticOrderTest not found with id " + id
                                )),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<Long, DiagnosticOrder> orderMap = orderTestMap.values().stream()
                .map(DiagnosticOrderTest::getOrderId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> orderRepository.findById(id)
                                .orElseThrow(() -> new BadRequestAlertException(
                                        "notfound",
                                        "diagnostic_orders",
                                        "Order not found with id " + id
                                )),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Long patientId = resolveSinglePatientId(orderMap);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "patients",
                        "Patient not found with id " + patientId
                ));

        DiagnosticOrderTest firstOrderTest = orderTestMap.values().iterator().next();

        String facilityName =
                reportCommonService.getFacilityNameFromDepartment(firstOrderTest.getReceivedDepartmentId());

        String departmentName =
                reportCommonService.getDepartmentName(firstOrderTest.getReceivedDepartmentId());

        String patientName =
                ((patient.getFirstName() != null ? patient.getFirstName() : "") + " " +
                        (patient.getLastName() != null ? patient.getLastName() : "")).trim();

        List<LaboratoryOrderSectionDTO> orderSections = orderMap.values().stream()
                .map(order -> buildOrderSection(order, results, orderTestMap))
                .toList();

        LOG.debug(
                "[LaboratoryResultReportService] GET_LABORATORY_RESULTS_REPORT - completed. patientId={} ordersCount={}",
                patientId,
                orderSections.size()
        );

        return new LaboratoryResultReportDTO(
                facilityName,
                departmentName,

                patientName,
                patient.getMedicalRecordNumber(),
                patient.getDateOfBirth(),
                reportCommonService.calculateAge(patient.getDateOfBirth()),
                patient.getSexAtBirth(),
                patient.getPrimaryMobileNumber(),

                orderSections
        );
    }

    private Long resolveSinglePatientId(Map<Long, DiagnosticOrder> orderMap) {
        List<Long> patientIds = orderMap.values().stream()
                .map(DiagnosticOrder::getPatientId)
                .distinct()
                .toList();

        if (patientIds.size() > 1) {
            throw new BadRequestAlertException(
                    "invalid_results",
                    "diagnostic_order_test_result",
                    "All selected results must belong to the same patient"
            );
        }

        return patientIds.get(0);
    }

    private LaboratoryOrderSectionDTO buildOrderSection(
            DiagnosticOrder order,
            List<DiagnosticOrderTestResult> allResults,
            Map<Long, DiagnosticOrderTest> orderTestMap
    ) {
        PatientEncounter encounter = encounterRepository.findById(order.getEncounterId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "patientEncounters",
                        "Encounter not found with id " + order.getEncounterId()
                ));

        String fromDepartment =
                reportCommonService.getDepartmentName(order.getFromDepartmentId());

        List<DiagnosticOrderTestResult> orderResults = allResults.stream()
                .filter(result -> {
                    DiagnosticOrderTest orderTest = orderTestMap.get(result.getOrderTestId());
                    return order.getId().equals(orderTest.getOrderId());
                })
                .toList();

        Map<Long, List<DiagnosticOrderTestResult>> byOrderTest =
                orderResults.stream()
                        .collect(Collectors.groupingBy(
                                DiagnosticOrderTestResult::getOrderTestId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<LaboratoryOrderTestSectionDTO> orderTestSections =
                byOrderTest.entrySet().stream()
                        .map(entry -> buildOrderTestSection(
                                orderTestMap.get(entry.getKey()),
                                entry.getValue()
                        ))
                        .toList();

        return new LaboratoryOrderSectionDTO(
                order.getId(),
                order.getOrderNumber(),
                encounter.getEncounterNumber(),
                fromDepartment,
                orderTestSections
        );
    }

    private LaboratoryOrderTestSectionDTO buildOrderTestSection(
            DiagnosticOrderTest orderTest,
            List<DiagnosticOrderTestResult> results
    ) {
        DiagnosticTest test = diagnosticTestRepository.findById(orderTest.getTestId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_tests",
                        "Diagnostic test not found with id " + orderTest.getTestId()
                ));

        String receivedDepartment =
                reportCommonService.getDepartmentName(orderTest.getReceivedDepartmentId());

        String categoryName = diagnosticTestLaboratoryRepository
                .findByTest_Id(orderTest.getTestId())
                .map(DiagnosticTestLaboratory::getCategory)
                .orElse(null);

        List<LaboratoryResultItemDTO> resultItems = results.stream()
                .map(result -> buildResultItem(result, reportCommonService.getLovDisplayValue(categoryName)))
                .toList();

        return new LaboratoryOrderTestSectionDTO(
                orderTest.getId(),
                test.getName(),
                receivedDepartment,
                resultItems
        );
    }

    private LaboratoryResultItemDTO buildResultItem(
            DiagnosticOrderTestResult result,
            String categoryName
    ) {
        DiagnosticTestProfile testProfile = diagnosticTestProfileRepository.findById(result.getProfileTestId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_tests_profile",
                        "Diagnostic test profile not found with id " + result.getProfileTestId()
                ));

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
                        : result.getResultValueNumber() != null
                        ? result.getResultValueNumber().toString()
                        : result.getResultValueText();

        return new LaboratoryResultItemDTO(
                result.getCreatedDate(),
                normalRangeValue,
                categoryName,
                testProfile.getName(),

                resultValue,
                reportCommonService.getLovDisplayValue(String.valueOf(testProfile.getResultUnit())),
                result.getMarker(),
                result.getReviewDate(),
                reportCommonService.getDisplayUserName(result.getReviewBy())
        );
    }
}