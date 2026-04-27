package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.RadiologyReportDTO;
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
            throw new RuntimeException("Failed to generate radiology PDF with Chromium: " + e.getMessage(), e);
        }
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