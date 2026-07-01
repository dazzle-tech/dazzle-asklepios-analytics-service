package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.SickLeaveReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SickLeaveReportPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final ReportPdfCommonService reportPdfCommonService;


    public byte[] generateSickLeaveReportPdf(SickLeaveReportDTO dto, String timezone, String lang) {
        if (dto == null) {
            return new byte[0];
        }
        boolean isArabic = "ar".equalsIgnoreCase(lang);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("labels", buildSickLeaveReportLabels(isArabic));
        context.setVariable("generatedAtDisplay", reportPdfCommonService.generatedAtDisplay(timezone));
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        String primaryColor = reportPdfCommonService.getPrimaryColor();
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/sick-leave-report.css"
        ).replace("__PRIMARY_COLOR__", primaryColor);

        context.setVariable("reportCss", css);
        String html = templateEngine.process("reports/sick-leave-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    private Map<String, String> buildSickLeaveReportLabels(boolean isArabic) {
        Map<String, String> labels = new HashMap<>();

        // Header
        labels.put("sickLeaveReport", isArabic ? "تقرير الإجازة المرضية" : "SICK LEAVE REPORT");
        labels.put("encounter", isArabic ? "رقم الزيارة" : "ENCOUNTER");
        labels.put("generatedAt", isArabic ? "تاريخ الإنشاء" : "Generated At");

        // Patient Information
        labels.put("patientInformation", isArabic ? "1. معلومات المريض" : "1. Patient Information");
        labels.put("patientName", isArabic ? "اسم المريض" : "Patient Name");
        labels.put("mrn", isArabic ? "رقم الملف" : "MRN");
        labels.put("gender", isArabic ? "الجنس" : "Gender");
        labels.put("dobAge", isArabic ? "تاريخ الميلاد / العمر" : "DOB / Age");

        // Visit Information
        labels.put("visitInformation", isArabic ? "2. معلومات الزيارة" : "2. Visit Information");
        labels.put("facility", isArabic ? "المؤسسة" : "Facility");
        labels.put("department", isArabic ? "القسم" : "Department");
        labels.put("visitDate", isArabic ? "تاريخ الزيارة" : "Visit Date");
        labels.put("attendingPhysician", isArabic ? "الطبيب المعالج" : "Attending Physician");
        labels.put("chiefComplaint", isArabic ? "الشكوى الرئيسية" : "Chief Complaint");
        labels.put("diagnosis", isArabic ? "التشخيص" : "Diagnosis");

        // Sick Leave Period
        labels.put("sickLeavePeriod", isArabic ? "3. مدة الإجازة المرضية" : "3. Sick Leave Period");
        labels.put("medicalLeaveAuthorization", isArabic ? "تصريح الإجازة المرضية" : "Medical Leave Authorization");
        labels.put("fromDate", isArabic ? "من تاريخ" : "From Date");
        labels.put("toDate", isArabic ? "إلى تاريخ" : "To Date");
        labels.put("totalDays", isArabic ? "إجمالي الأيام" : "Total Days");
        labels.put("day", isArabic ? "يوم" : "DAY");
        labels.put("days", isArabic ? "أيام" : "DAYS");

        // Notes
        labels.put("notesRecommendations", isArabic ? "4. الملاحظات والتوصيات" : "4. Notes & Recommendations");
        labels.put("noNotes", isArabic ? "لا توجد ملاحظات." : "No notes recorded.");

        // Footer
        labels.put("physicianSignatureStamp", isArabic ? "توقيع وختم الطبيب" : "Physician Signature & Stamp");
        labels.put("issuedBy", isArabic ? "صادر عن" : "Issued By");

        return labels;
    }
}