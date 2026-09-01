package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.SettlementReport.SettlementReportCriteriaDTO;
import com.dazzle.asklepios.service.dto.SettlementReport.SettlementReportDTO;
import com.dazzle.asklepios.service.dto.SettlementReport.SettlementReportRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SettlementReportPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final SettlementReportService settlementReportService;
    private final ReportPdfCommonService reportPdfCommonService;

    public byte[] generateSettlementReportPdf(
            SettlementReportRequestDTO request,
            String timezone,
            String lang
    ) {

        SettlementReportDTO dto =
                settlementReportService.getSettlementReport(request);

        boolean isArabic = "ar".equalsIgnoreCase(lang);

        Context context = new Context();

        context.setVariable("report", dto);

        context.setVariable(
                "generatedAtDisplay",
                reportPdfCommonService.generatedAtDisplay(timezone)
        );

        context.setVariable(
                "logo",
                reportPdfCommonService.getLogoBase64()
        );

        context.setVariable(
                "lang",
                isArabic ? "ar" : "en"
        );

        context.setVariable(
                "dir",
                isArabic ? "rtl" : "ltr"
        );

        context.setVariable(
                "isArabic",
                isArabic
        );

        context.setVariable(
                "labels",
                buildSettlementReportLabels(isArabic)
        );

        String css =
                reportPdfCommonService
                        .loadCss(
                                "templates/reports/styles/settlement-report.css"
                        )
                        .replace(
                                "__PRIMARY_COLOR__",
                                reportPdfCommonService.getPrimaryColor()
                        );

        context.setVariable(
                "reportCss",
                css
        );

        String html =
                templateEngine.process(
                        "reports/settlement-report",
                        context
                );

        return reportPdfCommonService.renderPdfWithChromium(html);
    }

    private Map<String, String> buildSettlementReportLabels(boolean isArabic) {

        Map<String, String> labels = new HashMap<>();

        labels.put(
                "settlementReport",
                isArabic ? "تقرير التسويات" : "SETTLEMENT REPORT"
        );

        labels.put(
                "reportCriteria",
                isArabic ? "معايير التقرير" : "Report Criteria"
        );

        labels.put(
                "settlementDetails",
                isArabic ? "تفاصيل التسويات" : "Settlement Details"
        );

        labels.put(
                "summary",
                isArabic ? "الملخص" : "Summary"
        );

        labels.put(
                "generatedAt",
                isArabic ? "تاريخ الإنشاء" : "Generated At"
        );

        labels.put(
                "insuranceCompany",
                isArabic ? "شركة التأمين" : "Insurance Company"
        );

        labels.put(
                "encounterType",
                isArabic ? "نوع الزيارة" : "Encounter Type"
        );

        labels.put(
                "dateFrom",
                isArabic ? "من تاريخ" : "Date From"
        );

        labels.put(
                "dateTo",
                isArabic ? "إلى تاريخ" : "Date To"
        );

        labels.put(
                "patientName",
                isArabic ? "اسم المريض" : "Patient Name"
        );

        labels.put(
                "patientId",
                isArabic ? "رقم المريض" : "Patient ID"
        );

        labels.put(
                "invoiceNumber",
                isArabic ? "رقم الفاتورة" : "Invoice Number"
        );

        labels.put(
                "settlementNo",
                isArabic ? "رقم التسوية" : "Settlement No."
        );

        labels.put(
                "settlementDate",
                isArabic ? "تاريخ التسوية" : "Settlement Date"
        );

        labels.put(
                "claimNo",
                isArabic ? "رقم المطالبة" : "Claim No."
        );

        labels.put(
                "claimDate",
                isArabic ? "تاريخ المطالبة" : "Claim Date"
        );

        labels.put(
                "billedAmount",
                isArabic ? "المبلغ المفوتر" : "Billed Amount"
        );

        labels.put(
                "approvedAmount",
                isArabic ? "المبلغ المعتمد" : "Approved Amount"
        );

        labels.put(
                "rejectedAmount",
                isArabic ? "المبلغ المرفوض" : "Rejected Amount"
        );

        labels.put(
                "patientShare",
                isArabic ? "حصة المريض" : "Patient Share"
        );

        labels.put(
                "insuranceAmount",
                isArabic ? "مبلغ التأمين" : "Insurance Amount"
        );

        labels.put(
                "paidAmount",
                isArabic ? "المبلغ المدفوع" : "Paid Amount"
        );

        labels.put(
                "outstandingAmount",
                isArabic ? "المبلغ المستحق" : "Outstanding Amount"
        );

        labels.put(
                "settlementStatus",
                isArabic ? "حالة التسوية" : "Settlement Status"
        );

        labels.put(
                "totalRecords",
                isArabic ? "عدد السجلات" : "Total Records"
        );

        labels.put(
                "totalBilled",
                isArabic ? "إجمالي المفوتر" : "Total Billed"
        );

        labels.put(
                "totalApproved",
                isArabic ? "إجمالي المعتمد" : "Total Approved"
        );

        labels.put(
                "totalRejected",
                isArabic ? "إجمالي المرفوض" : "Total Rejected"
        );

        labels.put(
                "totalPatientShare",
                isArabic ? "إجمالي حصة المريض" : "Total Patient Share"
        );

        labels.put(
                "totalInsuranceAmount",
                isArabic ? "إجمالي مبلغ التأمين" : "Total Insurance Amount"
        );

        labels.put(
                "totalPaid",
                isArabic ? "إجمالي المدفوع" : "Total Paid"
        );

        labels.put(
                "totalOutstanding",
                isArabic ? "إجمالي المستحق" : "Total Outstanding"
        );

        labels.put(
                "grandTotal",
                isArabic ? "الإجمالي" : "Grand Total"
        );

        labels.put(
                "noData",
                isArabic
                        ? "لا توجد بيانات مطابقة لمعايير البحث"
                        : "No settlement data found"
        );
        labels.put(
                "status",
                isArabic ? "الحالة" : "Status"
        );

        return labels;
    }
}