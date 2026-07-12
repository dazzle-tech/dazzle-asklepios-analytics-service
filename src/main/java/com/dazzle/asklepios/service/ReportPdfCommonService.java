package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.SystemConfigKey;
import com.dazzle.asklepios.repository.SystemConfigurationRepository;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class ReportPdfCommonService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SystemConfigurationRepository repository;
    @Value("${application.storage.cdn-base-url:https://asklepios.sfo3.cdn.digitaloceanspaces.com}")
    private String cdnBaseUrl;

    public ReportPdfCommonService(SystemConfigurationRepository repository) {
        this.repository = repository;
    }

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

    public byte[] renderEmptyPdf(String message) {
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        @page {
                            size: 120mm 55mm;
                            margin: 0;
                        }
                
                        html, body {
                            margin: 0;
                            padding: 0;
                            width: 120mm;
                            height: 55mm;
                        }
                
                        body {
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-family: Arial, sans-serif;
                            font-size: 14px;
                            color: #333;
                        }
                    </style>
                </head>
                <body>
                    <div>%s</div>
                </body>
                </html>
                """.formatted(message == null || message.isBlank() ? "No data found" : message);

        return renderPdfWithChromium(html);
    }


    public String publicUrl(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return null;
        }

        if (storageKey.startsWith("http://") || storageKey.startsWith("https://")) {
            return storageKey;
        }

        String base = cdnBaseUrl == null ? "" : cdnBaseUrl.replaceAll("/$", "");
        String key = storageKey.replaceAll("^/", "");

        return base + "/" + key;
    }

    public String getLogoBase64() {
        try {
            String storageKey = repository.findByConfigKey(SystemConfigKey.SIDEBAR_LOGO)
                    .map(config -> config.getConfigValue())
                    .filter(value -> value != null && !value.isBlank())
                    .orElse(null);

            if (storageKey != null) {
                try (InputStream inputStream = new java.net.URL(publicUrl(storageKey)).openStream()) {
                    byte[] bytes = inputStream.readAllBytes();
                    return Base64.getEncoder().encodeToString(bytes);
                }
            }

            return getDefaultLogoBase64();

        } catch (Exception e) {
            return getDefaultLogoBase64();
        }
    }

    private String getDefaultLogoBase64() {
        try {
            ClassPathResource resource = new ClassPathResource("static/logo.png");
            InputStream inputStream = resource.getInputStream();
            byte[] bytes = inputStream.readAllBytes();

            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return "";
        }
    }

    public String loadCss(String classpathFile) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathFile);

            return new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load CSS file: " + classpathFile,
                    e
            );
        }
    }

    public String getPrimaryColor() {
        return repository.findByConfigKey(SystemConfigKey.PRIMARY_COLOR)
                .map(config -> config.getConfigValue())
                .filter(value -> value != null && !value.isBlank())
                .orElse("#001f3f");
    }
}