package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.DentalTreatmentPlanReportView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DentalTreatmentPlanPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final DentalTreatmentPlanReportService reportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generate(Long planId, String lang, List<Long> attachmentIds) {
        boolean arabic = "ar".equalsIgnoreCase(lang);
        DentalTreatmentPlanReportView report = reportService.load(planId, arabic, attachmentIds);

        Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        context.setVariable("lang", report.getLang());
        context.setVariable("dir", report.getDir());
        context.setVariable("labels", labels(arabic));

        String css = reportPdfCommonService.loadCss("templates/reports/styles/dental-treatment-plan.css")
                .replace("__PRIMARY_COLOR__", reportPdfCommonService.getPrimaryColor());
        context.setVariable("reportCss", css);

        String html = templateEngine.process("reports/dental-treatment-plan", context);
        return reportPdfCommonService.renderPdfWithChromium(html, true);
    }

    private Map<String, String> labels(boolean arabic) {
        Map<String, String> labels = new HashMap<>();
        labels.put("title", arabic ? "خطة العلاج السني" : "Dental Treatment Plan");
        labels.put("plan", arabic ? "رقم الخطة" : "Plan");
        labels.put("patient", arabic ? "أ. المريض والخطة" : "A. Patient and plan");
        labels.put("chart", arabic ? "ب. مخطط الأسنان" : "B. Dental chart");
        labels.put("sessions", arabic ? "ج. الجلسات والعلاجات" : "C. Sessions and treatments");
        labels.put("estimate", arabic ? "د. تقدير خطة العلاج" : "D. Treatment plan estimate");
        labels.put("images", arabic ? "هـ. الصور المحددة" : "E. Selected images");
        labels.put("name", arabic ? "اسم المريض" : "Patient name");
        labels.put("mrn", arabic ? "رقم الملف" : "MRN");
        labels.put("dob", arabic ? "تاريخ الميلاد / العمر" : "DOB / Age");
        labels.put("gender", arabic ? "الجنس" : "Gender");
        labels.put("practitioner", arabic ? "الممارس" : "Practitioner");
        labels.put("encounter", arabic ? "زيارة المنشأ" : "Origin encounter");
        labels.put("created", arabic ? "تاريخ الإنشاء" : "Created");
        labels.put("updated", arabic ? "آخر تحديث" : "Updated");
        labels.put("generated", arabic ? "تاريخ الإصدار" : "Generated");
        labels.put("status", arabic ? "الحالة" : "Status");
        labels.put("planTitle", arabic ? "العنوان" : "Title");
        labels.put("legend", arabic ? "الأسنان المظللة مشمولة في علاجات هذه الخطة غير الملغاة." : "Highlighted teeth are covered by this plan's non-cancelled treatments.");
        labels.put("session", arabic ? "رقم الجلسة" : "Session Number");
        labels.put("area", arabic ? "السن / المنطقة" : "Tooth / Area");
        labels.put("surface", arabic ? "السطح" : "Surface");
        labels.put("cdt", arabic ? "رمز CDT" : "CDT");
        labels.put("procedure", arabic ? "خدمة الفوترة" : "Billing Service");
        labels.put("price", arabic ? "سعر الوحدة" : "Unit Price");
        labels.put("quantity", arabic ? "الكمية" : "Quantity");
        labels.put("discount", arabic ? "مبلغ الخصم" : "Discount Amount");
        labels.put("lineNet", arabic ? "صافي السطر" : "Net Total");
        labels.put("rowStatus", arabic ? "الحالة" : "Status");
        labels.put("gross", arabic ? "الإجمالي" : "Gross");
        labels.put("net", arabic ? "الصافي المقدر" : "Estimated Net Total");
        labels.put("estimateNote", arabic
                ? "هذا تقدير لخطة العلاج وليس فاتورة أو مطالبة مستحقة."
                : "This is a treatment plan estimate, not an invoice or an outstanding payment.");
        labels.put("noTreatments", arabic ? "لا توجد علاجات على هذه الخطة." : "This plan has no treatments.");
        labels.put("right", arabic ? "يمين" : "R");
        labels.put("left", arabic ? "يسار" : "L");
        return labels;
    }
}
