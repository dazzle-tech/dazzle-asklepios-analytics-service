package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.SickLeaveReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SickLeaveReportPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final SickLeaveReportService sickLeaveReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateSickLeaveReportPdf(Long encounterId,
                                             LocalDate fromDate,
                                             LocalDate toDate,
                                             String notes,
                                             String timezone) {

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
        context.setVariable("generatedAtDisplay", reportPdfCommonService.generatedAtDisplay(timezone));
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());

        String html = templateEngine.process("reports/sick-leave-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    public byte[] generateSickLeaveReportPdf(SickLeaveReportDTO dto, String timezone) {
        if (dto == null) {
            return new byte[0];
        }

        Context context = new Context();
        context.setVariable("report", dto);
        context.setVariable("generatedAtDisplay", reportPdfCommonService.generatedAtDisplay(timezone));
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        String css = reportPdfCommonService.loadCss(
                "templates/reports/styles/sick-leave-report.css"
        );

        context.setVariable("reportCss", css);
        String html = templateEngine.process("reports/sick-leave-report", context);

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    public byte[] generateSickLeaveReportPdf(Long encounterId,
                                             LocalDate fromDate,
                                             LocalDate toDate,
                                             String notes) {
        return generateSickLeaveReportPdf(encounterId, fromDate, toDate, notes, null);
    }

    public byte[] generateSickLeaveReportPdf(Long encounterId,
                                             LocalDate fromDate,
                                             LocalDate toDate) {
        return generateSickLeaveReportPdf(encounterId, fromDate, toDate, null, null);
    }
}