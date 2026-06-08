package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.LaboratoryResultReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class LaboratoryPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final DiagnosticOrderTestResultReportService diagnosticOrderTestResultReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateLaboratoryPdf(Long diagnosticTestResultId) {
        LaboratoryResultReportDTO dto =
                diagnosticOrderTestResultReportService.getLaboratoryResult(diagnosticTestResultId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/laboratory-result-report.css"
        );
        context.setVariable("reportCss", css);
        String html = templateEngine.process("reports/laboratory-result-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }


}