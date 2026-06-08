package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.NurseSummaryReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class NurseSummaryPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final NurseSummaryReportService nurseSummaryReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateNurseSummaryPdf(Long encounterId) {

        NurseSummaryReportDTO dto =
                nurseSummaryReportService.getNurseSummaryReport(encounterId);

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        context.setVariable("reportCss", reportPdfCommonService.loadCss(
                "templates/reports/styles/nurse-summary-report.css"
        ));
        String html = templateEngine.process("reports/nurse-summary-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }


}