package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.VisitReportDTO;
import com.microsoft.playwright.*;
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
public class VisitReportPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final VisitReportService visitReportService;

    public byte[] generateVisitReportPdf(Long encounterId) {

        VisitReportDTO dto = visitReportService.getVisitReport(encounterId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", getLogoBase64());

        String html = templateEngine.process("reports/visit-report", context);

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
                    new Page.SetContentOptions()
                            .setWaitUntil(WaitUntilState.NETWORKIDLE)
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
            throw new RuntimeException("Failed to generate visit report PDF with Chromium", e);
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