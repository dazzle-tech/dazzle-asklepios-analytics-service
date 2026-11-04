package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.prescription.PrescriptionMedicationDTO;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionPrintDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.List;
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
        List<PrescriptionMedicationDTO> medicationsWithNotes = filterMedicationsWithNotes(dto);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("medicationsWithNotes", medicationsWithNotes);
        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("labels", buildPrescriptionReportLabels(isArabic, !medicationsWithNotes.isEmpty()));

        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        context.setVariable("stamp", reportPdfCommonService.getPrescriptionStampBase64());
        String primaryColor = reportPdfCommonService.getPrimaryColor();
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/prescription-report.css"
        ).replace("__PRIMARY_COLOR__", primaryColor);

        context.setVariable("reportCss", css);
        String html = templateEngine.process("reports/prescription-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    static List<PrescriptionMedicationDTO> filterMedicationsWithNotes(PrescriptionPrintDTO dto) {
        if (dto == null || dto.medications() == null || dto.medications().isEmpty()) {
            return List.of();
        }

        return dto.medications().stream()
                .filter(PrescriptionPdfRenderService::hasMedicationNotesContent)
                .toList();
    }

    static boolean hasMedicationNotesContent(PrescriptionMedicationDTO med) {
        if (med == null) {
            return false;
        }

        return hasText(med.notes())
                || hasText(med.extraDocumentation())
                || hasText(med.parametersToMonitor())
                || hasText(med.refillInterval())
                || med.numberOfRefills() != null
                || hasText(med.refillsAndParameters());
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Map<String, String> buildPrescriptionReportLabels(boolean isArabic, boolean includeMedicationNotesSection) {
        Map<String, String> labels = new HashMap<>();

        labels.put("prescriptionReport", isArabic ? "تقرير الوصفة الطبية" : "PRESCRIPTION REPORT");
        labels.put("docId", isArabic ? "رقم المستند" : "DOC ID");
        labels.put("issueDate", isArabic ? "تاريخ الإصدار" : "Issue Date");
        labels.put("time", isArabic ? "الوقت" : "Time");

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

        labels.put("activeIngredient", isArabic ? "المادة الفعالة" : "Active Ingredient");
        labels.put("medications", isArabic ? "2. الأدوية" : "2. Medications");
        labels.put("medicationName", isArabic ? "اسم الدواء" : "Medication Name");
        labels.put("instructions", isArabic ? "التعليمات" : "Instructions");
        labels.put("duration", isArabic ? "المدة" : "Duration");
        labels.put("refills", isArabic ? "إعادات الصرف" : "Refills");
        labels.put("parametersToMonitor",
                isArabic ? "المعاملات للمراقبة" : "Parameters to Monitor");
        labels.put("administrationInstructions",
                isArabic ? "تعليمات الإعطاء" : "Administration Instructions");
        labels.put("indication", isArabic ? "التشخيص العلاجي" : "Indication");
        labels.put("notes", isArabic ? "ملاحظات" : "Notes");
        labels.put("extraDocumentation",
                isArabic ? "توثيق إضافي" : "Extra Documentation");
        labels.put("refillsAndParameters",
                isArabic ? "إعادات الصرف والمعاملات للمراقبة" : "Refills and Parameters to Monitor");
        labels.put("noMedications",
                isArabic ? "لا توجد أدوية موصوفة حالياً."
                        : "No active prescription medications found.");

        int section = 3;
        if (includeMedicationNotesSection) {
            labels.put(
                    "medicationNotesDocumentation",
                    section++ + ". " + (isArabic ? "ملاحظات وتوثيق الأدوية" : "Medication Notes & Documentation")
            );
        } else {
            labels.put(
                    "medicationNotesDocumentation",
                    isArabic ? "ملاحظات وتوثيق الأدوية" : "Medication Notes & Documentation"
            );
        }

        labels.put(
                "clinicalObservationsDiagnoses",
                section++ + ". " + (isArabic ? "الملاحظات السريرية والتشخيصات" : "Clinical Observations & Diagnoses")
        );
        labels.put(
                "allergies",
                section + ". " + (isArabic ? "الحساسية" : "Allergies")
        );
        labels.put("entryDate", isArabic ? "تاريخ الإدخال" : "Entry Date");
        labels.put("noDiagnoses",
                isArabic ? "لا توجد تشخيصات سريرية مسجلة لهذه الزيارة."
                        : "No clinical diagnoses recorded for this encounter.");
        labels.put("allergyType", isArabic ? "نوع الحساسية" : "Allergy Type");
        labels.put("allergen", isArabic ? "مسبب الحساسية" : "Allergen");
        labels.put("severity", isArabic ? "الدرجة" : "Severity");
        labels.put("noAllergies",
                isArabic ? "لا توجد حساسية مسجلة."
                        : "No allergies recorded.");

        labels.put("prescriberSignature",
                isArabic ? "توقيع الطبيب الواصف" : "Prescriber Signature");
        labels.put("digitallyAuthenticatedBy",
                isArabic ? "تم الاعتماد إلكترونياً بواسطة"
                        : "Digitally Authenticated By");

        return labels;
    }
}
