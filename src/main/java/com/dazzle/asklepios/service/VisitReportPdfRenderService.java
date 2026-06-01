package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.VisitReportDTO;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
@Service
@RequiredArgsConstructor
public class VisitReportPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final VisitReportService visitReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateVisitReportPdf(Long encounterId, String timezone) {

        VisitReportDTO dto = visitReportService.getVisitReport(encounterId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("generatedAtDisplay", reportPdfCommonService.generatedAtDisplay(timezone));
        context.setVariable("logo", getLogoBase64());

        String html = templateEngine.process("reports/visit-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
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