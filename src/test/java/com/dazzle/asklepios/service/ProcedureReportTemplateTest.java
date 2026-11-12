package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO;
import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renders the procedure report template without a database, for a cancelled request with no
 * workflow data, which is the path with the most empty values.
 */
class ProcedureReportTemplateTest {

    private static final String DASH = "—";
    private SpringTemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    @Test
    void cancelledRequestWithoutWorkflowData_rendersInEnglish() {
        String html = render(false);

        assertTrue(html.contains("class=\"watermark\""));
        assertTrue(html.contains("Patient changed their mind"));
        assertTrue(html.contains("No known allergies"));
        assertTrue(html.contains("No readings recorded during the procedure."));
        assertTrue(html.contains("No complications recorded"));
        assertFalse(html.contains("Preliminary report"), "Cancelled requests are not preliminary");
        assertFalse(html.contains("class=\"chart\""), "No chart without readings");
    }

    @Test
    void cancelledRequestWithoutWorkflowData_rendersInArabic() {
        String html = render(true);

        assertTrue(html.contains("dir=\"rtl\""));
        assertTrue(html.contains("تقرير الإجراء الطبي"));
        assertTrue(html.contains("ملغى"));
    }

    private String render(boolean arabic) {
        Context context = new Context();
        context.setVariable("report", cancelledReport());
        context.setVariable("logo", "");
        context.setVariable("lang", arabic ? "ar" : "en");
        context.setVariable("dir", arabic ? "rtl" : "ltr");
        context.setVariable("labels", ProcedureReportPdfRenderService.labels(arabic));
        context.setVariable("chartColors", Map.of("heartRate", "#000", "oxygen", "#000", "bloodPressure", "#000", "respiratory", "#000"));
        context.setVariable("reportCss", "");
        return templateEngine.process("reports/procedure-report", context);
    }

    private static ProcedureReportDTO cancelledReport() {
        VitalsSet noVitals = new VitalsSet(DASH, DASH, DASH, DASH);
        return new ProcedureReportDTO(
                new Header(7L, "PRC-000007", "Clinic", "Day Surgery", "Excision", "EX1", "Surgical", "Minor",
                        "Normal", "CANCELLED", "danger", false, true, "12/11/2026 10:00", null),
                new PatientInfo("Jane Doe", "P1", "Female", "01/01/1990", "36 Years", "E1", "12/11/2026", "Clinic", "OPD", DASH),
                List.of(),
                new Request(DASH, "12/11/2026 09:00", "OPD", DASH, DASH, DASH, DASH, "Arm", DASH, null, null),
                new Registration(false, DASH, DASH, DASH, DASH, null),
                List.of(new TimelineStep("stepRequested", "12/11/2026 09:00", null, "done", "success"),
                        new TimelineStep("stepCancelled", "12/11/2026 09:30", null, "done", "danger")),
                List.of(new Kpi("kpiDuration", DASH, null, "primary"),
                        new Kpi("kpiComplications", "0", null, "success")),
                List.of(),
                new PreAssessment(false, false, false, null, List.of(), 0, noVitals, DASH, DASH),
                new Performance(false, DASH, DASH, DASH, false, DASH, DASH, DASH, DASH, DASH, false,
                        DASH, null, null, null, null, null),
                new Monitoring(List.of(), null, DASH, DASH, DASH, DASH, DASH),
                List.of(),
                List.of(),
                List.of(),
                new PostCare(false, List.of(), null, noVitals, DASH, DASH, null, "neutral", null, List.of(), DASH,
                        null, false, "0 213.6", List.of(), 0, 0, false, false, null, null, null, DASH, DASH),
                List.of(),
                new Cancellation("12/11/2026 09:30", "Nurse", "Patient changed their mind"),
                List.of(new Signature("sigPerforming", DASH, null))
        );
    }
}
