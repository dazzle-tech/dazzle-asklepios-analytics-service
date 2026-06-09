package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.LaboratoryResultReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LaboratoryPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final DiagnosticOrderTestResultReportService diagnosticOrderTestResultReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateLaboratoryPdf(Long diagnosticTestResultId,String lang) {
        LaboratoryResultReportDTO dto =
                diagnosticOrderTestResultReportService.getLaboratoryResult(diagnosticTestResultId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        boolean isArabic = "ar".equalsIgnoreCase(lang);

        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("labels", buildLaboratoryResultReportLabels(isArabic));
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/laboratory-result-report.css"
        );
        context.setVariable("reportCss", css);
        String html = templateEngine.process("reports/laboratory-result-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }
    private Map<String, String> buildLaboratoryResultReportLabels(boolean isArabic) {
        Map<String, String> labels = new HashMap<>();

        // Header
        labels.put("laboratoryResult", isArabic ? "نتيجة المختبر" : "LABORATORY RESULT");
        labels.put("encounter", isArabic ? "رقم الزيارة" : "ENCOUNTER");
        labels.put("order", isArabic ? "رقم الطلب" : "ORDER");

        // Patient Identification
        labels.put("patientIdentification", isArabic ? "1. بيانات المريض" : "1. Patient Identification");
        labels.put("fullName", isArabic ? "الاسم الكامل" : "Full Name");
        labels.put("gender", isArabic ? "الجنس" : "Gender");
        labels.put("mrnNumber", isArabic ? "رقم الملف" : "MRN Number");
        labels.put("dobAge", isArabic ? "تاريخ الميلاد / العمر" : "D.O.B / Age");
        labels.put("mobileNumber", isArabic ? "رقم الجوال" : "Mobile Number");
        labels.put("fromDepartment", isArabic ? "القسم الطالب" : "From Department");
        labels.put("testName", isArabic ? "اسم الفحص" : "Test Name");
        labels.put("resultDate", isArabic ? "تاريخ النتيجة" : "Result Date");

        // Result Details
        labels.put("resultDetails", isArabic ? "2. تفاصيل النتيجة" : "2. Result Details");
        labels.put("test", isArabic ? "الفحص" : "Test");
        labels.put("result", isArabic ? "النتيجة" : "Result");
        labels.put("unit", isArabic ? "الوحدة" : "Unit");
        labels.put("normalRange", isArabic ? "المعدل الطبيعي" : "Normal Range");
        labels.put("marker", isArabic ? "المؤشر" : "Marker");

        // Review Information
        labels.put("reviewInformation", isArabic ? "3. معلومات المراجعة" : "3. Review Information");
        labels.put("reviewedDate", isArabic ? "تاريخ المراجعة" : "Reviewed Date");
        labels.put("reviewedBy", isArabic ? "تمت المراجعة بواسطة" : "Reviewed By");

        // Footer
        labels.put("labSignature", isArabic ? "توقيع المختبر" : "Lab Signature");
        labels.put("digitallyGeneratedReport", isArabic ? "تقرير منشأ إلكترونياً" : "Digitally Generated Report");

        // Empty / fallback
        labels.put("notAvailable", isArabic ? "—" : "—");

        return labels;
    }

}