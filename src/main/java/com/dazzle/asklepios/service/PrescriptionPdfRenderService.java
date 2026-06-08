package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.prescription.PrescriptionPrintDTO;
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
import java.text.SimpleDateFormat;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PrescriptionPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final PrescriptionReportService prescriptionReportService;
   private final ReportPdfCommonService reportPdfCommonService;
    public byte[] generatePrescriptionPdf(Long prescriptionId) {
        PrescriptionPrintDTO dto = prescriptionReportService.getPrescriptionPrint(prescriptionId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo",reportPdfCommonService.getLogoBase64());
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/prescription-report.css"
        );

        context.setVariable("reportCss", css);
        String html = templateEngine.process("reports/prescription-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

}