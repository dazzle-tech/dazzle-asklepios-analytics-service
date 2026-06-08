package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.RadiologyReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class RadiologyPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final DiagnosticOrderTestReportService diagnosticOrderTestReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateRadiologyPdf(Long diagnosticTestReportId) {
        RadiologyReportDTO dto = diagnosticOrderTestReportService.getRadiologyReport(diagnosticTestReportId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/radiology-report.css"
        );

        context.setVariable("reportCss", css);

        String html = templateEngine.process("reports/radiology-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }


}