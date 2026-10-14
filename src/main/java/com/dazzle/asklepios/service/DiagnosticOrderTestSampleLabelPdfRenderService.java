package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Analyzer;
import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.DiagnosticOrderTest;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import com.dazzle.asklepios.repository.AnalyzerRepository;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.DiagnosticOrderTestRepository;
import com.dazzle.asklepios.repository.DiagnosticTestLaboratoryRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.service.dto.DiagnosticOrderSampleLabelDTO;
import com.dazzle.asklepios.service.dto.DiagnosticOrderTestSampleLabelDTO;
import com.dazzle.asklepios.service.dto.GroupedSampleLabelDTO;
import com.dazzle.asklepios.service.dto.GroupedSampleLabelsPrintResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dazzle.asklepios.web.rest.Helper.BarcodeImageUtil.generateCode128BarcodeBase64;
import static com.dazzle.asklepios.web.rest.Helper.BarcodeImageUtil.generateQrBase64;

@Service
@RequiredArgsConstructor
public class DiagnosticOrderTestSampleLabelPdfRenderService {

    private static final Logger LOG =
            LoggerFactory.getLogger(DiagnosticOrderTestSampleLabelPdfRenderService.class);

    private final SpringTemplateEngine templateEngine;
    private final DiagnosticOrderTestCollectedSampleService sampleService;
    private final ReportPdfCommonService reportPdfCommonService;
    private final DiagnosticOrderTestRepository orderTestRepository;
    private final DiagnosticTestLaboratoryRepository laboratoryRepository;
    private final AnalyzerRepository analyzerRepository;
    private final ApLovValueRepository apLovValueRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateSampleLabelPdf(
            Long orderTestId,
            String lang,
            Integer copies
    ) {
        DiagnosticOrderTestSampleLabelDTO dto =
                sampleService.getSampleLabel(orderTestId);

        return renderSampleLabels(
                List.of(dto),
                lang,
                copies
        );
    }

    public byte[] generateAllSampleLabelsPdf(
            Long orderTestId,
            String lang,
            Integer copies
    ) {
        List<DiagnosticOrderTestSampleLabelDTO> sampleLabels =
                sampleService.getSampleLabelsByOrderTestId(orderTestId);

        if (sampleLabels.isEmpty()) {
            return reportPdfCommonService.renderEmptyPdf(
                    "No collected samples found"
            );
        }

        return renderSampleLabels(
                sampleLabels,
                lang,
                copies
        );
    }
    /**
     * Prints one label per group of selected order tests that share the same
     * (analyzer, tubeColor, tubeType). Tests with no analyzer assigned are
     * never printed - they're reported back as "missing definition" so the
     * UI can warn the user. Tests that have an analyzer but incomplete
     * tubeColor/tubeType still print, just standalone (never merged into a
     * group based on incomplete/missing tube info).
     * Each printed group's printNumber is {analyzerCode}{orderId} (no
     * separator), persisted on every order test within that group.
     */
    @Transactional
    public GroupedSampleLabelsPrintResult generateGroupedSampleLabelsPdf(
            List<Long> orderTestIds,
            String lang,
            Integer copies
    ) {
        if (orderTestIds == null || orderTestIds.isEmpty()) {
            return new GroupedSampleLabelsPrintResult(
                    reportPdfCommonService.renderEmptyPdf("No tests selected"),
                    List.of()
            );
        }

        Map<Long, DiagnosticOrderTest> orderTestById = orderTestRepository.findAllById(orderTestIds).stream()
                .collect(Collectors.toMap(DiagnosticOrderTest::getId, ot -> ot));

        List<Long> testIds = orderTestById.values().stream()
                .map(DiagnosticOrderTest::getTestId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, DiagnosticTestLaboratory> labByTestId = laboratoryRepository.findByTest_IdIn(testIds).stream()
                .filter(lab -> lab.getTest() != null)
                .collect(Collectors.toMap(lab -> lab.getTest().getId(), lab -> lab));

        LinkedHashMap<Long, DiagnosticOrderTestSampleLabelDTO> dtoByOrderTestId = new LinkedHashMap<>();
        List<String> missingDefinitionTestNames = new ArrayList<>();
        for (Long orderTestId : orderTestIds) {
            Optional<DiagnosticOrderTestSampleLabelDTO> dto = sampleService.tryGetSampleLabel(orderTestId);
            if (dto.isPresent()) {
                dtoByOrderTestId.put(orderTestId, dto.get());
            } else {
                DiagnosticOrderTest orderTest = orderTestById.get(orderTestId);
                String testName = orderTest != null
                        ? diagnosticTestRepository.findById(orderTest.getTestId())
                                .map(DiagnosticTest::getName)
                                .orElse("#" + orderTestId)
                        : "#" + orderTestId;
                missingDefinitionTestNames.add(testName);
                LOG.warn(
                        "[GroupedSampleLabel] Skipping orderTestId={} - no collected sample yet",
                        orderTestId
                );
            }
        }

        if (dtoByOrderTestId.isEmpty()) {
            return new GroupedSampleLabelsPrintResult(
                    reportPdfCommonService.renderEmptyPdf("No collected samples found"),
                    missingDefinitionTestNames
            );
        }

        // Tests with no analyzer assigned at all are never printed (missing definition).
        // Tests with an analyzer but incomplete tubeColor/tubeType still print - just
        // standalone, since they can't be reliably matched to any group without both.
        LinkedHashMap<String, List<Long>> groups = new LinkedHashMap<>();

        for (Long orderTestId : dtoByOrderTestId.keySet()) {
            DiagnosticOrderTest orderTest = orderTestById.get(orderTestId);
            DiagnosticTestLaboratory lab = orderTest != null ? labByTestId.get(orderTest.getTestId()) : null;

            if (lab == null || lab.getAnalyzerId() == null) {
                missingDefinitionTestNames.add(dtoByOrderTestId.get(orderTestId).testName());
                continue;
            }

            String groupKey = (lab.getTubeColor() != null && lab.getTubeType() != null)
                    ? lab.getAnalyzerId() + "|" + lab.getTubeColor() + "|" + lab.getTubeType()
                    : "STANDALONE-" + orderTestId;

            groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(orderTestId);
        }

        if (groups.isEmpty()) {
            LOG.warn(
                    "[GroupedSampleLabel] No eligible tests to print - all selected tests are missing analyzer/tube definition: {}",
                    missingDefinitionTestNames
            );
            return new GroupedSampleLabelsPrintResult(
                    reportPdfCommonService.renderEmptyPdf("Selected tests are missing analyzer/tube configuration"),
                    missingDefinitionTestNames
            );
        }

        Map<Long, Analyzer> analyzerById = analyzerRepository.findAllById(
                labByTestId.values().stream()
                        .map(DiagnosticTestLaboratory::getAnalyzerId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()
        ).stream().collect(Collectors.toMap(Analyzer::getId, a -> a));

        List<String> lovKeysToResolve = new ArrayList<>();
        analyzerById.values().forEach(a -> lovKeysToResolve.add(a.getType()));
        labByTestId.values().forEach(lab -> {
            lovKeysToResolve.add(lab.getTubeColor());
            lovKeysToResolve.add(lab.getTubeType());
        });
        Map<String, String> lovDisplayByKey = apLovValueRepository.findByKeyIn(
                lovKeysToResolve.stream().filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(ApLovValue::getKey, ApLovValue::getLovDisplayVale));

        List<GroupedSampleLabelDTO> labelsList = new ArrayList<>();
        List<DiagnosticOrderTest> toSave = new ArrayList<>();

        for (List<Long> groupOrderTestIds : groups.values()) {
            List<DiagnosticOrderTestSampleLabelDTO> groupDtos = groupOrderTestIds.stream()
                    .map(dtoByOrderTestId::get)
                    .toList();

            DiagnosticOrderTestSampleLabelDTO representative = groupDtos.get(0);

            String joinedNames = groupDtos.size() > 1
                    ? groupDtos.stream()
                            .map(DiagnosticOrderTestSampleLabelDTO::testName)
                            .filter(name -> name != null && !name.isBlank())
                            .distinct()
                            .collect(Collectors.joining(", "))
                    : representative.testName();

            // Reuse an already-assigned printNumber (from an earlier print) if any
            // member of the group has one - never recompute/overwrite it.
            String existingPrintNumber = groupOrderTestIds.stream()
                    .map(id -> orderTestById.get(id).getPrintNumber())
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            DiagnosticTestLaboratory lab = labByTestId.get(orderTestById.get(groupOrderTestIds.get(0)).getTestId());
            Analyzer analyzer = analyzerById.get(lab.getAnalyzerId());
            String analyzerCode = analyzer != null ? analyzer.getCode() : "";
            String printNumber = existingPrintNumber != null
                    ? existingPrintNumber
                    : analyzerCode + String.format("%07d", representative.orderId());

            GroupedSampleLabelDTO groupedLabel = new GroupedSampleLabelDTO(
                    representative.orderId(),
                    representative.patientName(),
                    representative.facilityName(),
                    representative.mrn(),
                    joinedNames,
                    representative.sampleDateTime(),
                    representative.expiryDate(),
                    analyzer != null ? lovDisplayByKey.get(analyzer.getType()) : null,
                    lovDisplayByKey.get(lab.getTubeType()),
                    lovDisplayByKey.get(lab.getTubeColor()),
                    printNumber
            );

            labelsList.add(groupedLabel);

            groupOrderTestIds.forEach(id -> {
                DiagnosticOrderTest orderTest = orderTestById.get(id);
                if (orderTest != null && orderTest.getPrintNumber() == null) {
                    orderTest.setPrintNumber(printNumber);
                    toSave.add(orderTest);
                }
            });
        }

        orderTestRepository.saveAll(toSave);

        if (!missingDefinitionTestNames.isEmpty()) {
            LOG.warn(
                    "[GroupedSampleLabel] {} test(s) skipped - missing analyzer/tube definition: {}",
                    missingDefinitionTestNames.size(),
                    missingDefinitionTestNames
            );
        }

        LOG.info(
                "[GroupedSampleLabel] Printed {} label(s) for {} selected order test(s)",
                labelsList.size(),
                orderTestIds.size()
        );

        return new GroupedSampleLabelsPrintResult(
                renderGroupedSampleLabels(labelsList, lang, copies),
                missingDefinitionTestNames
        );
    }

    public byte[] generateOrderSampleLabelPdf(
            Long orderId,
            String lang,
            Integer copies
    ) {

        int safeCopies = copies == null || copies < 1 ? 1 : copies;
        boolean isArabic = "ar".equalsIgnoreCase(lang);

        DiagnosticOrderSampleLabelDTO label =
                sampleService.getOrderSampleLabel(orderId);

        Context context = new Context();

        context.setVariable(
                "label",
                label
        );

        context.setVariable(
                "copies",
                safeCopies
        );

        context.setVariable(
                "today",
                DATE_FORMAT.format(LocalDate.now())
        );

        context.setVariable(
                "lang",
                isArabic ? "ar" : "en"
        );

        context.setVariable(
                "dir",
                isArabic ? "rtl" : "ltr"
        );

        context.setVariable(
                "labels",
                buildOrderSampleLabelLabels(isArabic)
        );

        context.setVariable(
                "reportCss",
                reportPdfCommonService.loadCss(
                        "templates/reports/styles/order-sample-label.css"
                )
        );

        context.setVariable(
                "service",
                this
        );

        String html = templateEngine.process(
                "reports/order-sample-label",
                context
        );

        return reportPdfCommonService.renderPdfWithChromium(
                html
        );
    }
    public String buildOrderBarcodeImage(
            DiagnosticOrderSampleLabelDTO dto
    ) {

        return generateCode128BarcodeBase64(
                nullSafe(String.valueOf(dto.orderId())),
                1000,
                200
        );
    }

    private byte[] renderSampleLabels(
            List<DiagnosticOrderTestSampleLabelDTO> sampleLabels,
            String lang,
            Integer copies
    ) {
        int safeCopies = copies == null || copies < 1 ? 1 : copies;
        boolean isArabic = "ar".equalsIgnoreCase(lang);

        Context context = new Context();

        context.setVariable("labelsList", sampleLabels);
        context.setVariable("copies", safeCopies);

        context.setVariable(
                "today",
                DATE_FORMAT.format(LocalDate.now())
        );

        context.setVariable(
                "lang",
                isArabic ? "ar" : "en"
        );

        context.setVariable(
                "dir",
                isArabic ? "rtl" : "ltr"
        );

        context.setVariable(
                "labels",
                buildSampleLabelLabels(isArabic)
        );

        context.setVariable(
                "reportCss",
                reportPdfCommonService.loadCss(
                        "templates/reports/styles/sample-label.css"
                )
        );

        context.setVariable("service", this);

        String html = templateEngine.process(
                "reports/sample-label",
                context
        );

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    private byte[] renderGroupedSampleLabels(
            List<GroupedSampleLabelDTO> sampleLabels,
            String lang,
            Integer copies
    ) {
        int safeCopies = copies == null || copies < 1 ? 1 : copies;
        boolean isArabic = "ar".equalsIgnoreCase(lang);

        Context context = new Context();

        context.setVariable("labelsList", sampleLabels);
        context.setVariable("copies", safeCopies);

        context.setVariable(
                "today",
                DATE_FORMAT.format(LocalDate.now())
        );

        context.setVariable(
                "lang",
                isArabic ? "ar" : "en"
        );

        context.setVariable(
                "dir",
                isArabic ? "rtl" : "ltr"
        );

        context.setVariable(
                "labels",
                buildGroupedSampleLabelLabels(isArabic)
        );

        context.setVariable(
                "reportCss",
                reportPdfCommonService.loadCss(
                        "templates/reports/styles/sample-label.css"
                )
        );

        context.setVariable("service", this);

        String html = templateEngine.process(
                "reports/grouped-sample-label",
                context
        );

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    public String formatSampleDateTime(
            GroupedSampleLabelDTO dto
    ) {
        return dto.sampleDateTime() != null
                ? DATE_TIME_FORMAT.format(
                dto.sampleDateTime()
                        .atZone(ZoneId.systemDefault())
        )
                : "—";
    }

    public String formatExpiryDate(
            GroupedSampleLabelDTO dto
    ) {
        return dto.expiryDate() != null
                ? DATE_TIME_FORMAT.format(
                dto.expiryDate()
                        .atZone(ZoneId.systemDefault())
        )
                : "";
    }

    public String buildQrImage(
            GroupedSampleLabelDTO dto
    ) {
        return generateQrBase64(
                nullSafe(dto.printNumber()),
                220,
                220
        );
    }

    public String buildBarcodeImage(
            GroupedSampleLabelDTO dto
    ) {
        return generateCode128BarcodeBase64(
                nullSafe(dto.printNumber()),
                1000,
                200
        );
    }

    private Map<String, String> buildGroupedSampleLabelLabels(
            boolean isArabic
    ) {
        Map<String, String> labels = new HashMap<>();

        labels.put("sampleLabel", isArabic ? "ملصق العينة" : "Sample Label");
        labels.put("patient", isArabic ? "المريض" : "Patient");
        labels.put("mrn", isArabic ? "رقم الملف" : "MRN");
        labels.put("sample", isArabic ? "تاريخ العينة" : "Sample");
        labels.put("expiry", isArabic ? "تاريخ الانتهاء" : "Expiry");
        labels.put("test", isArabic ? "الفحص" : "Test");
        labels.put("analyzerType", isArabic ? "نوع المحلل" : "Analyzer Type");
        labels.put("tubeType", isArabic ? "نوع الأنبوب" : "Tube Type");
        labels.put("tubeColor", isArabic ? "لون الأنبوب" : "Tube Color");

        return labels;
    }

    public String formatSampleDateTime(
            DiagnosticOrderTestSampleLabelDTO dto
    ) {
        return dto.sampleDateTime() != null
                ? DATE_TIME_FORMAT.format(
                dto.sampleDateTime()
                        .atZone(ZoneId.systemDefault())
        )
                : "—";
    }

    public String formatExpiryDate(
            DiagnosticOrderTestSampleLabelDTO dto
    ) {
        return dto.expiryDate() != null
                ? DATE_TIME_FORMAT.format(
                dto.expiryDate()
                        .atZone(ZoneId.systemDefault())
        )
                : "";
    }

    public String formatQuantity(BigDecimal value) {
        if (value == null) {
            return "—";
        }

        return value
                .stripTrailingZeros()
                .toPlainString();
    }

    public String buildQrImage(
            DiagnosticOrderTestSampleLabelDTO dto
    ) {
        String sampleDateTime = formatSampleDateTime(dto);
        String qrValue = buildQrValue(dto, sampleDateTime);

        return generateQrBase64(
                qrValue,
                220,
                220
        );
    }

    /**
     * Barcode contains printNumber (analyzerCode+orderId) instead of MRN.
     */
    public String buildBarcodeImage(
            DiagnosticOrderTestSampleLabelDTO dto
    ) {
        String barcodeValue =
                nullSafe(dto.printNumber())
                ;

        return generateCode128BarcodeBase64(
                barcodeValue,
                1000,
                200
        );
    }

    private String buildQrValue(
            DiagnosticOrderTestSampleLabelDTO dto,
            String sampleDateTime
    ) {
        return    nullSafe(dto.printNumber())
                ;
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank()
                ? "—"
                : value;
    }

    private Map<String, String> buildSampleLabelLabels(
            boolean isArabic
    ) {
        Map<String, String> labels = new HashMap<>();

        labels.put(
                "sampleLabel",
                isArabic ? "ملصق العينة" : "Sample Label"
        );

        labels.put(
                "patient",
                isArabic ? "المريض" : "Patient"
        );

        labels.put(
                "mrn",
                isArabic ? "رقم الملف" : "MRN"
        );

        labels.put(
                "sample",
                isArabic ? "تاريخ العينة" : "Sample"
        );

        labels.put(
                "expiry",
                isArabic ? "تاريخ الانتهاء" : "Expiry"
        );

        labels.put(
                "source",
                isArabic ? "مصدر العينة" : "Source"
        );

        labels.put(
                "test",
                isArabic ? "الفحص" : "Test"
        );

        labels.put(
                "amount",
                isArabic ? "الكمية" : "Amount"
        );

        return labels;
    }
    private Map<String, String> buildOrderSampleLabelLabels(
            boolean isArabic
    ) {

        Map<String, String> labels = new HashMap<>();

        labels.put(
                "sampleLabels",
                isArabic ? "ملصقات العينات" : "Sample Labels"
        );

        labels.put(
                "orderNumber",
                isArabic ? "رقم الطلب" : "Order No"
        );
        labels.put(
                "mrn",
                isArabic ? "رقم الملف" : "MRN"
        );

        return labels;
    }
}