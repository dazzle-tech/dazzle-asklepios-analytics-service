package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestCollectedSample;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestCollectedSampleRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.service.dto.DiagnosticOrderTestSampleLabelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DiagnosticOrderTestCollectedSampleService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderTestCollectedSampleService.class);

    private final DiagnosticOrderTestRepository orderTestRepository;
    private final DiagnosticOrderTestCollectedSampleRepository sampleRepository;
    private final DiagnosticOrderRepository orderRepository;
    private final PatientRepository patientRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final DepartmentsRepository departmentRepository;

    public DiagnosticOrderTestCollectedSampleService(DiagnosticOrderTestRepository orderTestRepository, DiagnosticOrderTestCollectedSampleRepository sampleRepository, DiagnosticOrderRepository orderRepository, PatientRepository patientRepository, DiagnosticTestRepository diagnosticTestRepository, DepartmentsRepository departmentRepository
    ) {

        this.orderTestRepository = orderTestRepository;
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.patientRepository = patientRepository;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.departmentRepository = departmentRepository;
    }


    public DiagnosticOrderTestSampleLabelDTO getSampleLabel(Long orderTestId) {

        LOG.debug("[SampleLabelService] GET_SAMPLE_LABEL - start. orderTestId={}", orderTestId);

        DiagnosticOrderTest orderTest = orderTestRepository.findById(orderTestId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_order_tests",
                        "DiagnosticOrderTest not found with id " + orderTestId
                ));

        DiagnosticOrderTestCollectedSample lastSample = sampleRepository
                .findTopByOrderTestIdOrderByCreatedDateDescIdDesc(orderTestId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "no_sample",
                        "diagnostic_order_test_collected_samples",
                        "No collected sample found for orderTestId " + orderTestId
                ));

        Long orderId = orderTest.getOrderId();
        if (orderId == null) {
            throw new BadRequestAlertException(
                    "invalid_order",
                    "diagnostic_order_tests",
                    "OrderId is null for orderTestId " + orderTestId
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

        DiagnosticTest test = diagnosticTestRepository.findById(orderTest.getTestId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_tests",
                        "Diagnostic test not found with id " + orderTest.getTestId()
                ));
        Department department = departmentRepository.findById(orderTest.getReceivedDepartmentId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_tests",
                        "Facility not found with id " + orderTest.getTestId()
                ));
        String patientName = (patient.getFirstName() + " " + patient.getLastName()).trim();
        String mrn = patient.getMedicalRecordNumber();

        String facilityName = department.getFacility().getName();

        LOG.debug(
                "[SampleLabelService] GET_SAMPLE_LABEL - data prepared. orderTestId={} patient={} test={}",
                orderTestId,
                patientName,
                test.getName()
        );

        return new DiagnosticOrderTestSampleLabelDTO(
                orderTestId,
                patientName,
                facilityName,
                mrn,
                test.getName(),
                lastSample.getCollectedAt(),
                lastSample.getQuantity(),
                lastSample.getUnit()
        );
    }
}
