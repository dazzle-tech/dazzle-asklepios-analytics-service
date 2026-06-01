package com.dazzle.asklepios.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class ReportPdfCommonService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ZoneId resolveZoneId(String timezone) {
        try {
            return timezone != null && !timezone.isBlank()
                    ? ZoneId.of(timezone)
                    : ZoneId.systemDefault();
        } catch (Exception e) {
            return ZoneId.systemDefault();
        }
    }

    public String generatedAtDisplay(String timezone) {
        ZoneId zoneId = resolveZoneId(timezone);

        return DATE_TIME_FORMATTER.format(
                Instant.now().atZone(zoneId)
        );
    }

    public byte[] renderPdfWithChromium(String html) {
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
            throw new RuntimeException("Failed to generate PDF with Chromium", e);
        }
    }
    public String getLogoBase64() {
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