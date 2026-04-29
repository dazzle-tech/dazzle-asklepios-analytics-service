package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.patientLabel.PatientLabelDTO;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.text.SimpleDateFormat;

import static com.dazzle.asklepios.web.rest.Helper.BarcodeImageUtil.generateCode128BarcodeBase64;
import static com.dazzle.asklepios.web.rest.Helper.BarcodeImageUtil.generateQrBase64;

@Service
@RequiredArgsConstructor
public class PatientLabelPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final PatientService patientService;

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

        String html = templateEngine.process("reports/patient-label", context);

        return renderPdfWithChromium(html);
    }

    private byte[] renderPdfWithChromium(String html) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            BrowserContext browserContext = browser.newContext();
            Page page = browserContext.newPage();

            page.setContent(
                    html,
                    new Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE)
            );

            byte[] pdfBytes = page.pdf(
                    new Page.PdfOptions()
                            .setPrintBackground(true)
                            .setPreferCSSPageSize(true)
            );

            browserContext.close();
            browser.close();

            return pdfBytes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate patient label PDF with Chromium: " + e.getMessage(), e);
        }
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