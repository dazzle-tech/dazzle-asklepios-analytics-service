package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.RadiologyReportDTO;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RadiologyPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final DiagnosticOrderTestReportService diagnosticOrderTestReportService;

    public byte[] generateRadiologyPdf(Long diagnosticTestReportId) {
        RadiologyReportDTO dto = diagnosticOrderTestReportService.getRadiologyReport(diagnosticTestReportId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", getLogoBase64());

        String html = templateEngine.process("reports/radiology-report", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ConverterProperties props = new ConverterProperties();
        HtmlConverter.convertToPdf(html, outputStream, props);

        return outputStream.toByteArray();
    }

    private String getLogoBase64() {
        try {
            ClassPathResource resource = new ClassPathResource("static/logo.png");
            InputStream inputStream = resource.getInputStream();
            byte[] bytes = inputStream.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return "";
        }
    }
}