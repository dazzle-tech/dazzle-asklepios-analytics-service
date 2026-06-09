package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.patient.PatientInformationReportDTO;
import com.dazzle.asklepios.service.dto.patientLabel.PatientLabelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.dazzle.asklepios.web.rest.Helper.BarcodeImageUtil.generateCode128BarcodeBase64;
import static com.dazzle.asklepios.web.rest.Helper.BarcodeImageUtil.generateQrBase64;

@Service
@RequiredArgsConstructor
public class PatientPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final PatientService patientService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generatePatientLabelPdf(Long patientId) {
        PatientLabelDTO dto = patientService.getPatientLabel(patientId);

        String dateOfBirthFormatted = dto.dateOfBirth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(dto.dateOfBirth())
                : "—";

        String qrValue = buildQrValue(dto, dateOfBirthFormatted);

        Context context = new Context();
        context.setVariable("label", dto);
        context.setVariable("dateOfBirthFormatted", dateOfBirthFormatted);
        context.setVariable("qrImage", generateQrBase64(qrValue, 260, 260));
        context.setVariable("barcodeImage", generateCode128BarcodeBase64(dto.mrn(), 620, 130));
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/patient-label.css"
        );
        context.setVariable("reportCss", css);
        String html = templateEngine.process("reports/patient-label", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    public byte[] generatePatientInformationPdf(Long patientId ,String lang) {

        PatientInformationReportDTO dto =
                patientService.getPatientInformationReport(patientId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        boolean isArabic = "ar".equalsIgnoreCase(lang);

        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("labels", buildPatientReportLabels(isArabic));
        context.setVariable("reportCss", reportPdfCommonService.loadCss(
                "templates/reports/styles/patient-information-report.css"
        ));
        String html = templateEngine.process("reports/patient-information-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    private String buildQrValue(PatientLabelDTO dto, String dateOfBirthFormatted) {
        return "MRN:" + nullSafe(dto.mrn())
                + ";NAME:" + nullSafe(dto.patientFullName())
                + ";DOB:" + nullSafe(dateOfBirthFormatted)
                + ";GENDER:" + nullSafe(dto.gender());
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
    private Map<String, String> buildPatientReportLabels(boolean isArabic) {
        Map<String, String> labels = new HashMap<>();

        // Header / Title
        labels.put("patientReport", isArabic ? "تقرير المريض" : "PATIENT REPORT");
        labels.put("registrationDate", isArabic ? "تاريخ التسجيل" : "Registration Date");

        // Patient Information
        labels.put("patientInformation", isArabic ? "1. معلومات المريض" : "1. Patient Information");
        labels.put("fullName", isArabic ? "الاسم الكامل" : "Full Name");
        labels.put("mrn", isArabic ? "رقم الملف" : "MRN");
        labels.put("gender", isArabic ? "الجنس" : "Gender");
        labels.put("dobAge", isArabic ? "تاريخ الميلاد / العمر" : "D.O.B / Age");

        // Contact Information
        labels.put("contactInformation", isArabic ? "2. معلومات التواصل" : "2. Contact Information");
        labels.put("mobile", isArabic ? "رقم الجوال" : "Mobile");
        labels.put("secondaryPhone", isArabic ? "الهاتف الثانوي" : "Secondary Phone");
        labels.put("email", isArabic ? "البريد الإلكتروني" : "Email");
        labels.put("city", isArabic ? "المدينة" : "City");
        labels.put("state", isArabic ? "المحافظة / الولاية" : "State");
        labels.put("country", isArabic ? "الدولة" : "Country");

        // Document Info
        labels.put("documentInfo", isArabic ? "3. معلومات الوثيقة" : "3. Document Info");
        labels.put("type", isArabic ? "النوع" : "Type");
        labels.put("number", isArabic ? "الرقم" : "Number");

        // Emergency Contact
        labels.put("emergencyContact", isArabic ? "4. جهة الاتصال للطوارئ" : "4. Emergency Contact");
        labels.put("name", isArabic ? "الاسم" : "Name");
        labels.put("relation", isArabic ? "صلة القرابة" : "Relation");
        labels.put("phone", isArabic ? "رقم الهاتف" : "Phone");

        // Insurance
        labels.put("insurance", isArabic ? "5. التأمين" : "5. Insurance");
        labels.put("provider", isArabic ? "مزود التأمين" : "Provider");
        labels.put("policy", isArabic ? "رقم البوليصة" : "Policy");
        labels.put("preferredDoctor", isArabic ? "الطبيب المفضل" : "Preferred Doctor");

        // Footer
        labels.put("authorizedSignature", isArabic ? "التوقيع المعتمد" : "Authorized Signature");
        labels.put("digitallyAuthenticatedBy", isArabic ? "تم الاعتماد إلكترونياً بواسطة" : "Digitally Authenticated By");

        return labels;
    }
}