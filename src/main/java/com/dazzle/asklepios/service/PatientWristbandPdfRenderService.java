package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.patient.PatientWristbandDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

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

    public byte[] generateWristbandPdf(Long patientId) {
        PatientWristbandDTO dto = patientService.getPatientWristband(patientId);

        String dateOfBirthFormatted = dto.dateOfBirth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(dto.dateOfBirth())
                : "—";

        String admissionDateTimeFormatted = dto.admissionDateTime() != null
                ? dto.admissionDateTime().format(DATE_TIME_FORMAT)
                : "—";

        String qrValue = buildQrValue(dto, dateOfBirthFormatted, admissionDateTimeFormatted);

        Context context = new Context();
        context.setVariable("wristband", dto);
        context.setVariable("dateOfBirthFormatted", dateOfBirthFormatted);
        context.setVariable("admissionDateTimeFormatted", admissionDateTimeFormatted);
        context.setVariable("qrImage", generateQrBase64(qrValue, 220, 220));
        context.setVariable("barcodeImage", generateCode128BarcodeBase64(dto.mrn(), 520, 110));
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
}