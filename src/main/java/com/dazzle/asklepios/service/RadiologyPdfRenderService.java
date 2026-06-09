package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.RadiologyReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RadiologyPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final DiagnosticOrderTestReportService diagnosticOrderTestReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateRadiologyPdf(Long diagnosticTestReportId, String lang) {
        RadiologyReportDTO dto = diagnosticOrderTestReportService.getRadiologyReport(diagnosticTestReportId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/radiology-report.css"
        );
        boolean isArabic = "ar".equalsIgnoreCase(lang);

        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("labels", buildRadiologyReportLabels(isArabic));
        context.setVariable("reportCss", css);

        String html = templateEngine.process("reports/radiology-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    private Map<String, String> buildRadiologyReportLabels(boolean isArabic) {
        Map<String, String> labels = new HashMap<>();

        // Header
        labels.put("radiologyReport", isArabic ? "تقرير الأشعة" : "RADIOLOGY REPORT");
        labels.put("encounter", isArabic ? "رقم الزيارة" : "ENCOUNTER");

        // Patient Identification
        labels.put("patientIdentification", isArabic ? "1. بيانات المريض" : "1. Patient Identification");
        labels.put("fullName", isArabic ? "الاسم الكامل" : "Full Name");
        labels.put("gender", isArabic ? "الجنس" : "Gender");
        labels.put("mrnNumber", isArabic ? "رقم الملف" : "MRN Number");
        labels.put("dobAge", isArabic ? "تاريخ الميلاد / العمر" : "D.O.B / Age");
        labels.put("mobileNumber", isArabic ? "رقم الجوال" : "Mobile Number");
        labels.put("orderingPhysician", isArabic ? "الطبيب الطالب" : "Ordering Physician");
        labels.put("fromDepartment", isArabic ? "القسم الطالب" : "From Department");
        labels.put("testName", isArabic ? "اسم الفحص" : "Test Name");

        // Order Details
        labels.put("orderDetails", isArabic ? "2. تفاصيل الطلب" : "2. Order Details");
        labels.put("encounterNumber", isArabic ? "رقم الزيارة" : "Encounter Number");
        labels.put("department", isArabic ? "القسم" : "Department");

        // Report
        labels.put("report", isArabic ? "3. التقرير" : "3. Report");
        labels.put("severityLevel", isArabic ? "درجة الخطورة" : "Severity Level");
        labels.put("noRadiologyNotes",
                isArabic ? "لا توجد ملاحظات أشعة مسجلة لهذا التقرير."
                        : "No radiology notes recorded for this report.");

        // Review & Approval
        labels.put("reviewApproval", isArabic ? "4. المراجعة والاعتماد" : "4. Review & Approval");
        labels.put("reviewedBy", isArabic ? "تمت المراجعة بواسطة" : "Reviewed By");
        labels.put("approvedBy", isArabic ? "تم الاعتماد بواسطة" : "Approved By");

        // Footer
        labels.put("radiologistSignature", isArabic ? "توقيع أخصائي الأشعة" : "Radiologist Signature");
        labels.put("digitallyAuthenticatedBy",
                isArabic ? "تم الاعتماد إلكترونياً بواسطة" : "Digitally Authenticated By");

        return labels;
    }
}