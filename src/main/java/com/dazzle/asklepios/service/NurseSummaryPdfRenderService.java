package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NurseSummaryPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final NurseSummaryReportService nurseSummaryReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateNurseSummaryPdf(Long encounterId ,String lang) {

        NurseSummaryReportDTO dto =
                nurseSummaryReportService.getNurseSummaryReport(encounterId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        boolean isArabic = "ar".equalsIgnoreCase(lang);

        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("labels", buildNurseSummaryReportLabels(isArabic));
        context.setVariable("reportCss", reportPdfCommonService.loadCss(
                "templates/reports/styles/nurse-summary-report.css"
        ));
        String html = templateEngine.process("reports/nurse-summary-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }
    private Map<String, String> buildNurseSummaryReportLabels(boolean isArabic) {
        Map<String, String> labels = new HashMap<>();

        // Header
        labels.put("nurseSummaryReport", isArabic ? "تقرير ملخص التمريض" : "NURSE SUMMARY REPORT");
        labels.put("encounter", isArabic ? "رقم الزيارة" : "ENCOUNTER");
        labels.put("generatedAt", isArabic ? "تاريخ الإنشاء" : "Generated At");

        // 1. Patient Information
        labels.put("patientInformation", isArabic ? "1. معلومات المريض" : "1. Patient Information");
        labels.put("fullName", isArabic ? "الاسم الكامل" : "Full Name");
        labels.put("mrn", isArabic ? "رقم الملف" : "MRN");
        labels.put("gender", isArabic ? "الجنس" : "Gender");
        labels.put("dateOfBirthAge", isArabic ? "تاريخ الميلاد / العمر" : "Date of Birth / Age");

        // 2. Encounter Information
        labels.put("encounterInformation", isArabic ? "2. معلومات الزيارة" : "2. Encounter Information");
        labels.put("facility", isArabic ? "المركز الطبي" : "Facility");
        labels.put("department", isArabic ? "القسم" : "Department");
        labels.put("encounterDate", isArabic ? "تاريخ الزيارة" : "Encounter Date");
        labels.put("encounterNumber", isArabic ? "رقم الزيارة" : "Encounter Number");
        labels.put("priority", isArabic ? "الأولوية" : "Priority");
        labels.put("chiefComplaint", isArabic ? "الشكوى الرئيسية" : "Chief Complaint");
        labels.put("encounterReason", isArabic ? "سبب الزيارة" : "Encounter Reason");

        // 3. Active Allergies
        labels.put("activeAllergies", isArabic ? "3. الحساسية " : "3. Active Allergies");
        labels.put("allergenType", isArabic ? "نوع الحساسية" : "Allergen Type");
        labels.put("severity", isArabic ? "الدرجة" : "Severity");
        labels.put("allergen", isArabic ? "مسبب الحساسية" : "Allergen");
        labels.put("noActiveAllergies", isArabic ? "لا توجد حساسية  مسجلة." : "No active allergies recorded.");

        // 4. Active Warnings
        labels.put("activeWarnings", isArabic ? "4. التحذيرات " : "4. Active Warnings");
        labels.put("warningType", isArabic ? "نوع التحذير" : "Warning Type");
        labels.put("warning", isArabic ? "التحذير" : "Warning");
        labels.put("actionTaken", isArabic ? "الإجراء المتخذ" : "Action Taken");
        labels.put("note", isArabic ? "ملاحظة" : "Note");
        labels.put("noActiveWarnings", isArabic ? "لا توجد تحذيرات نشطة مسجلة." : "No active warnings recorded.");

        // 5. Clinical Observation
        labels.put("clinicalObservation", isArabic ? "5. الملاحظة السريرية" : "5. Clinical Observation");
        labels.put("reasonOfVisit", isArabic ? "سبب الزيارة" : "Reason of Visit");
        labels.put("diagnosis", isArabic ? "التشخيص" : "Diagnosis");
        labels.put("patientConditions", isArabic ? "حالة المريض" : "Patient Conditions");
        labels.put("functionalStatus", isArabic ? "الحالة الوظيفية" : "Functional Status");
        labels.put("cognitiveCheck", isArabic ? "التقييم الإدراكي" : "Cognitive Check");

        // 6. Vital Signs And Pain Assessment
        labels.put("vitalSignsAndPainAssessment",
                isArabic ? "6. العلامات الحيوية وتقييم الألم" : "6. Vital Signs And Pain Assessment");

        labels.put("bloodPressure", isArabic ? "ضغط الدم" : "Blood Pressure");
        labels.put("heartRate", isArabic ? "معدل النبض" : "Heart Rate");
        labels.put("temperature", isArabic ? "درجة الحرارة" : "Temperature");
        labels.put("oxygenSaturation", isArabic ? "تشبع الأكسجين" : "Oxygen Saturation");
        labels.put("respiratoryRate", isArabic ? "معدل التنفس" : "Respiratory Rate");
        labels.put("measurementSite", isArabic ? "مكان القياس" : "Measurement Site");

        labels.put("painDegree", isArabic ? "درجة الألم" : "Pain Degree");
        labels.put("painLevel", isArabic ? "مستوى الألم" : "Pain Level");
        labels.put("painPattern", isArabic ? "نمط الألم" : "Pain Pattern");
        labels.put("painDescription", isArabic ? "وصف الألم" : "Pain Description");

        // 7. Body Measurements
        labels.put("bodyMeasurements", isArabic ? "7. قياسات الجسم" : "7. Body Measurements");
        labels.put("weight", isArabic ? "الوزن" : "Weight");
        labels.put("height", isArabic ? "الطول" : "Height");
        labels.put("headCircumference", isArabic ? "محيط الرأس" : "Head Circumference");

        // Footer
        labels.put("nurseSignature", isArabic ? "توقيع الممرض/ة" : "Nurse Signature");
        labels.put("digitallyAuthenticatedBy",
                isArabic ? "تم الاعتماد إلكترونياً بواسطة" : "Digitally Authenticated By");

        return labels;
    }

}