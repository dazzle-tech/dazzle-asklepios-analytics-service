package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.enumeration.DiagnosticOrderTestStatus;
import com.dazzle.asklepios.domain.enumeration.PrescriptionStatus;
import com.dazzle.asklepios.domain.enumeration.ProcStatus;
import com.dazzle.asklepios.repository.DiagnosticOrderRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PatientPrescriptionMedicationRepository;
import com.dazzle.asklepios.repository.PatientPrescriptionRepository;
import com.dazzle.asklepios.repository.PatientProcedureRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.service.dto.reports.BrandMedicationsDTO;
import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import com.dazzle.asklepios.service.dto.reports.OrderedDiagnosticsDTO;
import com.dazzle.asklepios.service.dto.reports.ProceduresDTO;
import com.dazzle.asklepios.service.dto.reports.VisitReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitReportService {

    private final NurseSummaryReportService nurseSummaryReportService;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticOrderTestRepository diagnosticOrderTestRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final PatientPrescriptionRepository patientPrescriptionRepository;
    private final PatientPrescriptionMedicationRepository patientPrescriptionMedicationRepository;
    private final PatientProcedureRepository patientProcedureRepository;
    private final ProcedureRepository procedureRepository;

    public VisitReportDTO getVisitReport(Long encounterId) {

        NurseSummaryReportDTO nurseSummary =
                nurseSummaryReportService.getNurseSummaryReport(encounterId);

        if (nurseSummary == null) {
            return null;
        }

        List<OrderedDiagnosticsDTO> diagnosticsOrder =
                diagnosticOrderRepository
                        .findByEncounterId(encounterId)
                        .stream()
                        .flatMap(order ->
                                diagnosticOrderTestRepository
                                        .findByOrderIdOrderByIdAsc(order.getId())
                                        .stream()
                                        .filter(test -> test.getStatus() != DiagnosticOrderTestStatus.CANCELLED)
                                        .map(test -> {
                                            DiagnosticTest diagnosticTest = diagnosticTestRepository
                                                    .findById(test.getTestId())
                                                    .orElse(null);

                                            return new OrderedDiagnosticsDTO(
                                                    order.getOrderNumber(),
                                                    diagnosticTest.getName(),
                                                    test.getOrderType() != null              // 👈 add this
                                                            ? test.getOrderType().name()
                                                            : null


                                            );
                                        })
                        )
                        .toList();

        List<BrandMedicationsDTO> medications =
                patientPrescriptionRepository
                        .findByEncounterIdOrderByCreatedDateAsc(encounterId)
                        .stream()
                        .filter(rx -> rx.getStatus() != PrescriptionStatus.CANCELLED)
                        .flatMap(rx ->
                                patientPrescriptionMedicationRepository
                                        .findByPrescriptionHeader_IdOrderByIdAsc(rx.getId())
                                        .stream()
                                        .filter(m -> m.getStatus() != PrescriptionStatus.CANCELLED)
                                        .map(m -> {
                                            BrandMedication brand = m.getMedications();
                                            return new BrandMedicationsDTO(
                                                    brand != null ? brand.getName() : null,
                                                    brand != null ? brand.getCode() : null,
                                                    m.getInstructions(),
                                                    m.getInstructionsType() != null ? m.getInstructionsType().name() : null
                                            );
                                        })
                        )
                        .toList();

        List<ProceduresDTO> procedures =
                patientProcedureRepository
                        .findByEncounter_IdOrderByCreatedDateAsc(encounterId)
                        .stream()
                        .filter(p -> p.getStatus() != ProcStatus.CANCELLED)
                        .map(p -> {
                            Procedure proc = procedureRepository
                                    .findById(p.getProcedureId())
                                    .orElse(null);

                            return new ProceduresDTO(
                                    proc != null ? proc.getName() : null,
                                    proc != null ? proc.getCode() : null,
                                    proc != null ? proc.getCategoryType() : null,
                                    p.getNotes()
                            );
                        })
                        .toList();
        return new VisitReportDTO(
                nurseSummary.patientInfo(),
                nurseSummary.encounterInfo(),
                nurseSummary.observation(),
                nurseSummary.vitalSigns(),
                nurseSummary.bodyMeasurements(),
                nurseSummary.additionalMeasurements(),
                nurseSummary.allergies(),
                nurseSummary.warnings(),
                diagnosticsOrder,
                medications,
                procedures,
                null,
                Instant.now()
        );
    }
}