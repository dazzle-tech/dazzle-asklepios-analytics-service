package com.dazzle.asklepios.integration.ai.service;

import com.dazzle.asklepios.domain.DiagnosticOrder;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticOrderTestReport;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.integration.ai.client.RadiologyTemplateClient;
import com.dazzle.asklepios.integration.ai.client.dto.radiology.SelectAndFillRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.radiology.SelectAndFillResponseDTO;
import com.dazzle.asklepios.integration.ai.controller.vm.SelectAndFillRequestVM;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestReportRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RadiologyAutofillAiService {
    private static final Logger LOG = LoggerFactory.getLogger(RadiologyAutofillAiService.class);

    private final RadiologyTemplateClient client;

    private final PatientRepository patientRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticOrderTestReportRepository diagnosticOrderTestReportRepository;

    public SelectAndFillResponseDTO selectAndFill(
            SelectAndFillRequestVM request
    ) {

        DiagnosticOrderTestReport report =
                diagnosticOrderTestReportRepository
                        .findById(request.reportId())
                        .orElseThrow(() -> new BadRequestAlertException(
                                "Radiology report not found",
                                "diagnostic_order_test_report",
                                "notfound"
                        ));

        DiagnosticOrderTest orderTest =
                diagnosticOrderTestRepository
                        .findById(report.getOrderTestId())
                        .orElseThrow(() -> new BadRequestAlertException(
                                "Order test not found",
                                "diagnostic_order_test",
                                "notfound"
                        ));

        DiagnosticOrder order =
                diagnosticOrderRepository
                        .findById(orderTest.getOrderId())
                        .orElseThrow(() -> new BadRequestAlertException(
                                "Diagnostic order not found",
                                "diagnostic_order",
                                "notfound"
                        ));

        Patient patient =
                patientRepository
                        .findById(order.getPatientId())
                        .orElseThrow(() -> new BadRequestAlertException(
                                "Patient not found",
                                "patient",
                                "notfound"
                        ));

        SelectAndFillRequestDTO aiRequest =
                buildRequest(
                        report,
                        patient,
                        request.outputLanguage()
                );
        LOG.debug("AI Request DTO={}", aiRequest);

        return client.selectAndFill(aiRequest);
    }

    private SelectAndFillRequestDTO buildRequest(
            DiagnosticOrderTestReport report,
            Patient patient,
            String outputLanguage
    ) {

        return new SelectAndFillRequestDTO(
                UUID.randomUUID().toString(),
                report.getReport(),
                false,
                null,
                3,
                buildPatientContext(patient ,report),
                outputLanguage
        );
    }

    private Map<String, Object> buildPatientContext(
            Patient patient ,DiagnosticOrderTestReport report
    ) {

        Map<String, Object> context = new HashMap<>();

        context.put(
                "age",
                calculateAge(patient)
        );

        context.put(
                "sex",
                patient.getSexAtBirth() != null
                        ? patient.getSexAtBirth().name()
                        : null
        );
        context.put("severity", report.getSeverity());

        return context;
    }

    private Integer calculateAge(
            Patient patient
    ) {

        if (patient.getDateOfBirth() == null) {
            return null;
        }

        return Period.between(
                patient.getDateOfBirth()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate(),
                LocalDate.now()
        ).getYears();
    }
}
