package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.SickLeaveReportDTO;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SickLeaveReportPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final SickLeaveReportService sickLeaveReportService;

    public byte[] generateSickLeaveReportPdf(Long encounterId,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              String notes) {

        SickLeaveReportDTO dto = sickLeaveReportService.getSickLeaveReport(encounterId, fromDate, toDate);

        if (dto != null && notes != null) {
            dto = new SickLeaveReportDTO(
                    dto.patientInfo(),
                    dto.encounterInfo(),
                    dto.diagnosis(),
                    notes,
                    dto.sickLeaveFromDate(),
                    dto.sickLeaveToDate(),
                    dto.numberOfDays(),
                    dto.physicianFullName(),
                    dto.physicianSpecialty(),
                    dto.generatedAt()
            );
        }

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", getLogoBase64());

        String html = templateEngine.process("reports/sick-leave-report", context);

        return renderPdfWithChromium(html);
    }

    // New helper: render directly from a prepared DTO (useful when controller injects notes)
    public byte[] generateSickLeaveReportPdf(SickLeaveReportDTO dto) {
        if (dto == null) {
            return new byte[0];
        }

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", getLogoBase64());

        String html = templateEngine.process("reports/sick-leave-report", context);

        return renderPdfWithChromium(html);
    }

    // Backward-compatible overload: keep existing 3-arg signature and delegate to new method
    public byte[] generateSickLeaveReportPdf(Long encounterId,
                                              LocalDate fromDate,
                                              LocalDate toDate) {
        return generateSickLeaveReportPdf(encounterId, fromDate, toDate, null);
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
            throw new RuntimeException("Failed to generate sick leave report PDF with Chromium", e);
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
