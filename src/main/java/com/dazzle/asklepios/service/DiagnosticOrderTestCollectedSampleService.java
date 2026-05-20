package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestCollectedSample;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestCollectedSampleRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.service.dto.DiagnosticOrderTestSampleLabelDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DiagnosticOrderTestCollectedSampleService {

    private static final Logger LOG =
            LoggerFactory.getLogger(DiagnosticOrderTestCollectedSampleService.class);

    private final DiagnosticOrderTestRepository orderTestRepository;
    private final DiagnosticOrderTestCollectedSampleRepository sampleRepository;
    private final DiagnosticOrderRepository orderRepository;
    private final PatientRepository patientRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final DepartmentsRepository departmentRepository;
    private final ApLovValueRepository apLovValueRepository;

    public DiagnosticOrderTestCollectedSampleService(
            DiagnosticOrderTestRepository orderTestRepository,
            DiagnosticOrderTestCollectedSampleRepository sampleRepository,
            DiagnosticOrderRepository orderRepository,
            PatientRepository patientRepository,
            DiagnosticTestRepository diagnosticTestRepository,
            DepartmentsRepository departmentRepository,
            ApLovValueRepository apLovValueRepository
    ) {
        this.orderTestRepository = orderTestRepository;
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.patientRepository = patientRepository;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.departmentRepository = departmentRepository;
        this.apLovValueRepository = apLovValueRepository;
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

        return buildSampleLabelDto(orderTest, lastSample);
    }

    public DiagnosticOrderTestSampleLabelDTO getSampleLabelBySampleId(Long sampleId) {

        LOG.debug("[SampleLabelService] GET_SAMPLE_LABEL_BY_SAMPLE_ID - start. sampleId={}", sampleId);

        DiagnosticOrderTestCollectedSample sample = getSampleById(sampleId);

        DiagnosticOrderTest orderTest = orderTestRepository.findById(sample.getOrderTestId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_order_tests",
                        "DiagnosticOrderTest not found with id " + sample.getOrderTestId()
                ));

        return buildSampleLabelDto(orderTest, sample);
    }

    private DiagnosticOrderTestCollectedSample getSampleById(Long sampleId) {

        return sampleRepository.findById(sampleId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "no_sample",
                        "diagnostic_order_test_collected_samples",
                        "Sample not found with id " + sampleId
                ));
    }

    private DiagnosticOrderTestSampleLabelDTO buildSampleLabelDto(
            DiagnosticOrderTest orderTest,
            DiagnosticOrderTestCollectedSample sample
    ) {
        Long orderId = orderTest.getOrderId();

        if (orderId == null) {
            throw new BadRequestAlertException(
                    "invalid_order",
                    "diagnostic_order_tests",
                    "OrderId is null for orderTestId " + orderTest.getId()
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
                        "departments",
                        "Department not found with id " + orderTest.getReceivedDepartmentId()
                ));

        String patientName = ((patient.getFirstName() != null ? patient.getFirstName() : "")
                + " "
                + (patient.getLastName() != null ? patient.getLastName() : "")).trim();

        String facilityName = department.getFacility() != null
                ? department.getFacility().getName()
                : null;

        String sourceOfSample = sample.getSourceOfSample() != null
                ? apLovValueRepository.findById(sample.getSourceOfSample())
                .map(ApLovValue::getLovDisplayVale)
                .orElse(null)
                : null;

        LOG.debug(
                "[SampleLabelService] BUILD_SAMPLE_LABEL_DTO - data prepared. orderTestId={} sampleId={} patient={} test={}",
                orderTest.getId(),
                sample.getId(),
                patientName,
                test.getName()
        );

        return new DiagnosticOrderTestSampleLabelDTO(
                orderTest.getId(),
                patientName,
                facilityName,
                patient.getMedicalRecordNumber(),
                test.getName(),
                sample.getCollectedAt(),
                sample.getQuantity(),
                sample.getUnit(),
                sample.getExpiryDate(),
                sourceOfSample
        );
    }
    public List<DiagnosticOrderTestSampleLabelDTO> getSampleLabelsByOrderTestId(Long orderTestId) {
        DiagnosticOrderTest orderTest = orderTestRepository.findById(orderTestId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notfound",
                        "diagnostic_order_tests",
                        "DiagnosticOrderTest not found with id " + orderTestId
                ));

        List<DiagnosticOrderTestCollectedSample> samples =
                sampleRepository.findAllByOrderTestIdOrderByCreatedDateDescIdDesc(orderTestId);

        if (samples.isEmpty()) {
            throw new BadRequestAlertException(
                    "no_sample",
                    "diagnostic_order_test_collected_samples",
                    "No collected samples found for orderTestId " + orderTestId
            );
        }

        return samples.stream()
                .map(sample -> buildSampleLabelDto(orderTest, sample))
                .toList();
    }
}