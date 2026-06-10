package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.patient.PatientWristbandDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.dazzle.asklepios.web.rest.Helper.BarcodeImageUtil.generateCode128BarcodeBase64;
import static com.dazzle.asklepios.web.rest.Helper.BarcodeImageUtil.generateQrBase64;

@Service
@RequiredArgsConstructor
public class PatientWristbandPdfRenderService {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final SpringTemplateEngine templateEngine;
    private final PatientService patientService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateWristbandPdf(Long patientId, String lang, Integer copies) {
        PatientWristbandDTO dto = patientService.getPatientWristband(patientId);

        int safeCopies = copies == null || copies < 1 ? 1 : copies;

        String dateOfBirthFormatted = dto.dateOfBirth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(dto.dateOfBirth())
                : "—";

        String admissionDateTimeFormatted = dto.admissionDateTime() != null
                ? dto.admissionDateTime().format(DATE_TIME_FORMAT)
                : "—";

        String qrValue = buildQrValue(dto, dateOfBirthFormatted, admissionDateTimeFormatted);

        boolean isArabic = "ar".equalsIgnoreCase(lang);

        Context context = new Context();
        context.setVariable("wristband", dto);
        context.setVariable("dateOfBirthFormatted", dateOfBirthFormatted);
        context.setVariable("admissionDateTimeFormatted", admissionDateTimeFormatted);
        context.setVariable("qrImage", generateQrBase64(qrValue, 220, 220));
        context.setVariable("barcodeImage", generateCode128BarcodeBase64(dto.mrn(), 520, 110));

        context.setVariable("copies", safeCopies);

        context.setVariable("lang", isArabic ? "ar" : "en");
        context.setVariable("dir", isArabic ? "rtl" : "ltr");
        context.setVariable("labels", buildPatientWristbandLabels(isArabic));

        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/patient-wristband.css"
        );

        context.setVariable("reportCss", css);

        String html = templateEngine.process("reports/patient-wristband", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    private String buildQrValue(PatientWristbandDTO dto,
                                String dateOfBirthFormatted,
                                String admissionDateTimeFormatted) {
        return "MRN:" + nullSafe(dto.mrn())
                + ";NAME:" + nullSafe(dto.fullName())
                + ";DOB:" + nullSafe(dateOfBirthFormatted)
                + ";GENDER:" + nullSafe(dto.gender())
                + ";BLOOD_GROUP:" + nullSafe(dto.bloodGroup())
                + ";ALLERGY:" + nullSafe(dto.allergyAlert())
                + ";ADMISSION_DT:" + nullSafe(admissionDateTimeFormatted)
                + ";FACILITY:" + nullSafe(dto.facilityName());
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
    private Map<String, String> buildPatientWristbandLabels(boolean isArabic) {
        Map<String, String> labels = new HashMap<>();

        labels.put("patientWristband",
                isArabic ? "سوار المريض" : "Patient Wristband");

        labels.put("name",
                isArabic ? "الاسم" : "Name");

        labels.put("mrn",
                isArabic ? "رقم الملف" : "MRN");

        labels.put("dob",
                isArabic ? "تاريخ الميلاد" : "DOB");

        labels.put("gender",
                isArabic ? "الجنس" : "Gender");

        labels.put("bloodGroup",
                isArabic ? "فصيلة الدم" : "Blood Group");

        labels.put("allergy",
                isArabic ? "الحساسية" : "Allergy");

        return labels;
    }
}