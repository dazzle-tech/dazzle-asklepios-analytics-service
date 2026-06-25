package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.VisitReportDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VisitReportPdfRenderService {
    private static final Logger LOG = LoggerFactory.getLogger(VisitReportPdfRenderService.class);
    private final SpringTemplateEngine templateEngine;
    private final VisitReportService visitReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateVisitReportPdf(Long encounterId, String timezone, String lang) {

        VisitReportDTO dto = visitReportService.getVisitReport(encounterId);

        boolean isArabic = "ar".equalsIgnoreCase(lang);
        String primaryColor = reportPdfCommonService.getPrimaryColor();

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("generatedAtDisplay", reportPdfCommonService.generatedAtDisplay(timezone));
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("isArabic", isArabic);
        context.setVariable("labels", buildVisitReportLabels(isArabic));
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/visit-report.css"
        ).replace("__PRIMARY_COLOR__", primaryColor);

        context.setVariable("reportCss", css);

        String html = templateEngine.process("reports/visit-report", context);
        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    private Map<String, String> buildVisitReportLabels(boolean isArabic) {
        Map<String, String> labels = new HashMap<>();

        labels.put("visitReport", isArabic ? "تقرير الزيارة" : "VISIT REPORT");
        labels.put("encounter", isArabic ? "رقم الزيارة" : "ENCOUNTER");

        labels.put("patientInformation", isArabic ? "1. معلومات المريض" : "1. Patient Information");
        labels.put("patientName", isArabic ? "اسم المريض" : "Patient Name");
        labels.put("mrn", isArabic ? "رقم الملف" : "MRN");
        labels.put("gender", isArabic ? "الجنس" : "Gender");
        labels.put("dobAge", isArabic ? "تاريخ الميلاد / العمر" : "DOB / Age");
        labels.put("facility", isArabic ? "المركز الطبي" : "Facility");
        labels.put("department", isArabic ? "القسم" : "Department");
        labels.put("visitDate", isArabic ? "تاريخ الزيارة" : "Visit Date");

        labels.put("activeAllergies", isArabic ? "2. الحساسية " : "2. Active Allergies");
        labels.put("allergenType", isArabic ? "نوع الحساسية" : "Allergen Type");
        labels.put("allergen", isArabic ? "مسبب الحساسية" : "Allergen");
        labels.put("severity", isArabic ? "الدرجة" : "Severity");
        labels.put("noAllergies", isArabic ? "لا يوجد حساسية مسجلة." : "No active allergies recorded.");

        labels.put("activeWarnings", isArabic ? "3. التحذيرات " : "3. Active Warnings");
        labels.put("warningType", isArabic ? "نوع التحذير" : "Warning Type");
        labels.put("warning", isArabic ? "التحذير" : "Warning");
        labels.put("noWarnings", isArabic ? "لا يوجد تحذيرات مسجلة." : "No active warnings recorded.");

        labels.put("clinicalVisit", isArabic ? "4. الزيارة السريرية" : "4. Clinical Visit");
        labels.put("chiefComplaint", isArabic ? "الشكوى الطبية" : "Chief Complaint");
        labels.put("primaryDiagnosis", isArabic ? "التشخيص " : "Primary Diagnosis");
        labels.put("plan", isArabic ? "الخطة" : "Plan");

        labels.put("observationsSummary", isArabic ? "5. ملخص الملاحظات" : "5. Observations Summary");
        labels.put("bloodPressure", isArabic ? "ضغط الدم" : "Blood Pressure");
        labels.put("heartRate", isArabic ? "معدل النبض" : "Heart Rate");
        labels.put("temperature", isArabic ? "الحرارة" : "Temperature");
        labels.put("oxygenSaturation", isArabic ? "تشبع الأكسجين" : "Oxygen Saturation");
        labels.put("respiratoryRate", isArabic ? "معدل التنفس" : "Respiratory Rate");
        labels.put("painDegree", isArabic ? "درجة الألم" : "Pain Degree");
        labels.put("conditions", isArabic ? "الحالة" : "Conditions");
        labels.put("weight", isArabic ? "الوزن" : "Weight");
        labels.put("height", isArabic ? "الطول" : "Height");
        labels.put("headCircumference", isArabic ? "محيط الرأس" : "Head Circumference");

        labels.put("orderedDiagnostics", isArabic ? "6. الفحوصات المطلوبة" : "6. Ordered Diagnostics");
        labels.put("orderNumber", isArabic ? "رقم الطلب" : "Order #");
        labels.put("testName", isArabic ? "اسم الفحص" : "Test Name");
        labels.put("type", isArabic ? "النوع" : "Type");
        labels.put("noDiagnostics", isArabic ? "لا يوجد فحوصات مطلوبة." : "No diagnostics ordered.");

        labels.put("prescriptionMedications", isArabic ? "7. الأدوية الموصوفة" : "7. Prescription Medications");
        labels.put("medication", isArabic ? "الدواء" : "Medication");
        labels.put("instructions", isArabic ? "التعليمات" : "Instructions");
        labels.put("duration", isArabic ? "المدة" : "Duration");
        labels.put("refill", isArabic ? "إعادة صرف" : "Refill");
        labels.put("indication", isArabic ? "التشخيص" : "Indication");
        labels.put("yes", isArabic ? "نعم" : "Yes");
        labels.put("no", isArabic ? "لا" : "No");
        labels.put("days", isArabic ? "أيام" : "days");
        labels.put("noMedications", isArabic ? "لا يوجد أدوية موصوفة." : "No medications prescribed.");

        labels.put("procedures", isArabic ? "8. الإجراءات" : "8. Procedures");
        labels.put("procedure", isArabic ? "الإجراء" : "Procedure");
        labels.put("code", isArabic ? "الكود" : "Code");
        labels.put("category", isArabic ? "التصنيف" : "Category");
        labels.put("notes", isArabic ? "ملاحظات" : "Notes");
        labels.put("noProcedures", isArabic ? "لا يوجد إجراءات مسجلة." : "No procedures recorded.");

        labels.put("physicianSignature", isArabic ? "توقيع الطبيب" : "Physician Signature");
        labels.put("digitallyAuthenticatedBy", isArabic ? "تم الاعتماد إلكترونياً بواسطة" : "Digitally Authenticated By");

        return labels;
    }


}