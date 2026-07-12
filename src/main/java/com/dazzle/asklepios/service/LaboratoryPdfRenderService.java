package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.laboratory.LaboratoryResultReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LaboratoryPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final DiagnosticOrderTestResultReportService diagnosticOrderTestResultReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateLaboratoryPdf(List<Long> resultIds, String lang, String timezone) {
        LaboratoryResultReportDTO dto =
                diagnosticOrderTestResultReportService.getLaboratoryResults(resultIds,timezone);

        boolean isArabic = "ar".equalsIgnoreCase(lang);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("labels", buildLaboratoryResultReportLabels(isArabic));
        String primaryColor = reportPdfCommonService.getPrimaryColor();
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/laboratory-result-report.css"
        ).replace("__PRIMARY_COLOR__", primaryColor);

        context.setVariable("reportCss", css);

        String html = templateEngine.process(
                "reports/laboratory-result-report",
                context
        );

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    private Map<String, String> buildLaboratoryResultReportLabels(boolean isArabic) {
        Map<String, String> labels = new HashMap<>();

        labels.put("laboratoryResult", isArabic ? "نتائج المختبر" : "LABORATORY RESULTS");

        labels.put("patientIdentification", isArabic ? "1. بيانات المريض" : "1. Patient Identification");
        labels.put("fullName", isArabic ? "الاسم الكامل" : "Full Name");
        labels.put("gender", isArabic ? "الجنس" : "Gender");
        labels.put("mrnNumber", isArabic ? "رقم الملف" : "MRN Number");
        labels.put("dobAge", isArabic ? "تاريخ الميلاد / العمر" : "D.O.B / Age");
        labels.put("mobileNumber", isArabic ? "رقم الجوال" : "Mobile Number");

        labels.put("resultDetails", isArabic ? "2. تفاصيل النتائج" : "2. Result Details");
        labels.put("order", isArabic ? "رقم الطلب" : "Order");
        labels.put("encounter", isArabic ? "رقم الزيارة" : "Encounter");
        labels.put("fromDepartment", isArabic ? "القسم الطالب" : "From Department");
        labels.put("receivedDepartment", isArabic ? "القسم المستلم" : "Received Department");
        labels.put("orderTest", isArabic ? "الفحص المطلوب" : "Order Test");

        labels.put("test", isArabic ? "الفحص" : "Test");
        labels.put("category", isArabic ? "التصنيف" : "Category");
        labels.put("result", isArabic ? "النتيجة" : "Result");
        labels.put("unit", isArabic ? "الوحدة" : "Unit");
        labels.put("normalRange", isArabic ? "المعدل الطبيعي" : "Normal Range");
        labels.put("marker", isArabic ? "المؤشر" : "Marker");
        labels.put("resultDate", isArabic ? "تاريخ النتيجة" : "Result Date");
        labels.put("reviewedDate", isArabic ? "تاريخ المراجعة" : "Reviewed Date");
        labels.put("reviewedBy", isArabic ? "تمت المراجعة بواسطة" : "Reviewed By");

        labels.put("labSignature", isArabic ? "توقيع المختبر" : "Lab Signature");
        labels.put("digitallyGeneratedReport", isArabic ? "تقرير منشأ إلكترونياً" : "Digitally Generated Report");
        labels.put("notAvailable", "—");
        return labels;
    }
}