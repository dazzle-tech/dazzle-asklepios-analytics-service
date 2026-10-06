package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.vaccination.PatientVaccinationReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PatientVaccinationReportPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final PatientVaccinationReportService patientVaccinationReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generatePatientVaccinationReportPdf(Long patientId, String timezone, String lang) {
        PatientVaccinationReportDTO dto =
                patientVaccinationReportService.getPatientVaccinationReport(patientId);

        boolean isArabic = "ar".equalsIgnoreCase(lang);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("zoneId", reportPdfCommonService.resolveZoneId(timezone));
        context.setVariable("printedAtDisplay", reportPdfCommonService.generatedAtDisplay(timezone));
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());

        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("labels", buildVaccinationReportLabels(isArabic));

        String primaryColor = reportPdfCommonService.getPrimaryColor();
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/patient-vaccination-report.css"
        ).replace("__PRIMARY_COLOR__", primaryColor);

        context.setVariable("reportCss", css);
        String html = templateEngine.process("reports/patient-vaccination-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    private Map<String, String> buildVaccinationReportLabels(boolean isArabic) {
        Map<String, String> labels = new HashMap<>();

        // Header
        labels.put("vaccinationReport", isArabic ? "تقرير التطعيمات" : "VACCINATION REPORT");
        labels.put("printDateTime", isArabic ? "تاريخ ووقت الطباعة" : "Print Date/Time");

        // Patient Information
        labels.put("patientInformation", isArabic ? "1. معلومات المريض" : "1. Patient Information");
        labels.put("name", isArabic ? "الاسم" : "Name");
        labels.put("mrn", isArabic ? "رقم الملف" : "MRN");
        labels.put("gender", isArabic ? "الجنس" : "Gender");
        labels.put("dob", isArabic ? "تاريخ الميلاد" : "DOB");
        labels.put("age", isArabic ? "العمر" : "Age");

        // Vaccines
        labels.put("vaccines", isArabic ? "2. التطعيمات" : "2. Vaccines");
        labels.put("atcCode", isArabic ? "رمز ATC" : "ATC Code");
        labels.put("type", isArabic ? "النوع" : "Type");
        labels.put("numberOfDoses", isArabic ? "عدد الجرعات" : "Number Of Doses");
        labels.put("roa", isArabic ? "طريقة الإعطاء" : "ROA");
        labels.put("siteOfAdministration", isArabic ? "موضع الإعطاء" : "Site Of Administration");
        labels.put("noVaccines", isArabic ? "لا توجد تطعيمات مسجلة لهذا المريض." : "No vaccinations recorded for this patient.");

        // Doses
        labels.put("brandName", isArabic ? "الاسم التجاري" : "Brand Name");
        labels.put("doseNumber", isArabic ? "رقم الجرعة" : "Dose Number");
        labels.put("dateOfAdministration", isArabic ? "تاريخ الإعطاء" : "Date Of Administration");
        labels.put("vaccinationLocation", isArabic ? "مكان التطعيم" : "Vaccination Location");

        // Footer
        labels.put("authorizedSignature", isArabic ? "التوقيع المعتمد" : "Authorized Signature");

        return labels;
    }
}
