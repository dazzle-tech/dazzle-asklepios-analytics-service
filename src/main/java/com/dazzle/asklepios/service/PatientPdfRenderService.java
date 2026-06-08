package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.patient.PatientInformationReportDTO;
import com.dazzle.asklepios.service.dto.patientLabel.PatientLabelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.text.SimpleDateFormat;

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

    public byte[] generatePatientInformationPdf(Long patientId) {

        PatientInformationReportDTO dto =
                patientService.getPatientInformationReport(patientId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
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
}