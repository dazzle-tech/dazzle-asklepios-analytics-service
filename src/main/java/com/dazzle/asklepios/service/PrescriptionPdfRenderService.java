package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.prescription.PrescriptionPrintDTO;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrescriptionPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final PrescriptionReportService prescriptionReportService;
   private final ReportPdfCommonService reportPdfCommonService;
    public byte[] generatePrescriptionPdf(Long prescriptionId, String lang) {
        PrescriptionPrintDTO dto = prescriptionReportService.getPrescriptionPrint(prescriptionId);
        boolean isArabic = "ar".equalsIgnoreCase(lang);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("labels", buildPrescriptionReportLabels(isArabic));

        context.setVariable("logo",reportPdfCommonService.getLogoBase64());
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/prescription-report.css"
        );

        context.setVariable("reportCss", css);
        String html = templateEngine.process("reports/prescription-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }
    private Map<String, String> buildPrescriptionReportLabels(boolean isArabic) {
        Map<String, String> labels = new HashMap<>();

        // Header
        labels.put("prescriptionReport", isArabic ? "تقرير الوصفة الطبية" : "PRESCRIPTION REPORT");
        labels.put("docId", isArabic ? "رقم المستند" : "DOC ID");
        labels.put("issueDate", isArabic ? "تاريخ الإصدار" : "Issue Date");
        labels.put("time", isArabic ? "الوقت" : "Time");

        // Patient Identification
        labels.put("patientIdentification", isArabic ? "1. بيانات المريض" : "1. Patient Identification");
        labels.put("fullName", isArabic ? "الاسم الكامل" : "Full Name");
        labels.put("gender", isArabic ? "الجنس" : "Gender");
        labels.put("mrnNumber", isArabic ? "رقم الملف" : "MRN Number");
        labels.put("dobAge", isArabic ? "تاريخ الميلاد / العمر" : "D.O.B / Age");

        labels.put("encounterDepartment", isArabic ? "قسم الزيارة" : "Encounter Department");
        labels.put("urgency", isArabic ? "درجة الاستعجال" : "Urgency");
        labels.put("encounterId", isArabic ? "رقم الزيارة" : "Encounter ID");
        labels.put("encounterDate", isArabic ? "تاريخ الزيارة" : "Encounter Date");

        labels.put("phone", isArabic ? "رقم الهاتف" : "Phone");
        labels.put("email", isArabic ? "البريد الإلكتروني" : "Email");
        labels.put("insurance", isArabic ? "التأمين" : "Insurance");
        labels.put("primaryDocument", isArabic ? "المستند الأساسي" : "Primary Document");

        // Diagnoses
        labels.put("clinicalObservationsDiagnoses",
                isArabic ? "2. الملاحظات السريرية والتشخيصات" : "2. Clinical Observations & Diagnoses");
        labels.put("entryDate", isArabic ? "تاريخ الإدخال" : "Entry Date");
        labels.put("noDiagnoses",
                isArabic ? "لا توجد تشخيصات سريرية مسجلة لهذه الزيارة."
                        : "No clinical diagnoses recorded for this encounter.");

        // Allergies
        labels.put("allergies", isArabic ? "3. الحساسية" : "3. Allergies");
        labels.put("allergyType", isArabic ? "نوع الحساسية" : "Allergy Type");
        labels.put("allergen", isArabic ? "مسبب الحساسية" : "Allergen");
        labels.put("severity", isArabic ? "الدرجة" : "Severity");
        labels.put("noAllergies",
                isArabic ? "لا توجد حساسية مسجلة."
                        : "No allergies recorded.");

        // Warnings
        labels.put("warnings", isArabic ? "4. التحذيرات" : "4. Warnings");
        labels.put("warningType", isArabic ? "نوع التحذير" : "Warning Type");
        labels.put("warning", isArabic ? "التحذير" : "Warning");
        labels.put("noWarnings",
                isArabic ? "لا توجد تحذيرات مسجلة."
                        : "No warnings recorded.");

        // Medications
        labels.put("medications", isArabic ? "5. الأدوية" : "5. Medications");
        labels.put("medicationName", isArabic ? "اسم الدواء" : "Medication Name");
        labels.put("instructions", isArabic ? "التعليمات" : "Instructions");
        labels.put("duration", isArabic ? "المدة" : "Duration");
        labels.put("refills", isArabic ? "إعادات الصرف" : "Refills");
        labels.put("administrationInstructions",
                isArabic ? "تعليمات الإعطاء" : "Administration Instructions");
        labels.put("indication", isArabic ? "الدلالة العلاجية" : "Indication");
        labels.put("noMedications",
                isArabic ? "لا توجد أدوية موصوفة حالياً."
                        : "No active prescription medications found.");

        // Footer
        labels.put("prescriberSignature",
                isArabic ? "توقيع الطبيب الواصف" : "Prescriber Signature");

        labels.put("digitallyAuthenticatedBy",
                isArabic ? "تم الاعتماد إلكترونياً بواسطة"
                        : "Digitally Authenticated By");

        return labels;
    }
}