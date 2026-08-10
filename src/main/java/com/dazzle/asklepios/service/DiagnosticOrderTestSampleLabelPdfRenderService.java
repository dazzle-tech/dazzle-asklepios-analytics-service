package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.DiagnosticOrderTestSampleLabelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dazzle.asklepios.web.rest.Helper.BarcodeImageUtil.generateCode128BarcodeBase64;
import static com.dazzle.asklepios.web.rest.Helper.BarcodeImageUtil.generateQrBase64;

@Service
@RequiredArgsConstructor
public class DiagnosticOrderTestSampleLabelPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final DiagnosticOrderTestCollectedSampleService sampleService;
    private final ReportPdfCommonService reportPdfCommonService;

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
     * Barcode contains ORDER TEST ID instead of MRN.
     */
    public String buildBarcodeImage(
            DiagnosticOrderTestSampleLabelDTO dto
    ) {
        String barcodeValue =
                nullSafe(dto.orderId().toString())
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
        return    nullSafe(dto.orderId().toString())
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
}