package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.reports.procedure.ProcedureReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcedureReportPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final ProcedureReportService procedureReportService;
    private final ReportPdfCommonService reportPdfCommonService;
    private final ReportVerificationService reportVerificationService;

    public ProcedureReportDTO getReport(Long procedureRequestId, String timezone, String lang) {
        return procedureReportService.load(
                procedureRequestId,
                reportPdfCommonService.resolveZoneId(timezone),
                "ar".equalsIgnoreCase(lang)
        );
    }

    /**
     * @param verifyBaseUrl origin of the UI the report is printed from; when given, the report
     *                      carries a QR code linking to the public verification page
     */
    public byte[] generateProcedureReportPdf(Long procedureRequestId, String timezone, String lang, String verifyBaseUrl) {
        boolean arabic = "ar".equalsIgnoreCase(lang);
        ZoneId zone = reportPdfCommonService.resolveZoneId(timezone);
        ProcedureReportDTO report = procedureReportService.load(procedureRequestId, zone, arabic);

        Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("verification", verification(procedureRequestId, verifyBaseUrl));
        context.setVariable("logo", reportPdfCommonService.getLogoBase64());
        context.setVariable("lang", arabic ? "ar" : "en");
        context.setVariable("dir", arabic ? "rtl" : "ltr");
        context.setVariable("labels", labels(arabic));
        context.setVariable("chartColors", Map.of(
                "heartRate", ProcedureVitalsChartRenderer.HEART_RATE_COLOR,
                "oxygen", ProcedureVitalsChartRenderer.OXYGEN_COLOR,
                "bloodPressure", ProcedureVitalsChartRenderer.BLOOD_PRESSURE_COLOR,
                "respiratory", ProcedureVitalsChartRenderer.RESPIRATORY_COLOR
        ));

        String css = reportPdfCommonService.loadCss("templates/reports/styles/procedure-report.css")
                .replace("__PRIMARY_COLOR__", reportPdfCommonService.getPrimaryColor());
        context.setVariable("reportCss", css);

        String html = templateEngine.process("reports/procedure-report", context);
        return reportPdfCommonService.renderPdfWithChromium(html, true);
    }

    private Map<String, String> verification(Long procedureRequestId, String verifyBaseUrl) {
        String token = reportVerificationService.sign(procedureRequestId, Instant.now());
        String url = reportVerificationService.verificationUrl(verifyBaseUrl, token);
        if (url == null) {
            return null;
        }
        String svg = reportVerificationService.qrSvg(url);
        return svg == null ? null : Map.of("url", url, "qrSvg", svg);
    }

    static Map<String, String> labels(boolean ar) {
        Map<String, String> l = new HashMap<>();

        // Header
        l.put("title", ar ? "تقرير الإجراء الطبي" : "Procedure Report");
        l.put("docId", ar ? "رقم المستند" : "Doc ID");
        l.put("printedAt", ar ? "تاريخ الطباعة" : "Printed");
        l.put("printedBy", ar ? "طُبع بواسطة" : "Printed by");
        l.put("code", ar ? "الرمز" : "Code");
        l.put("category", ar ? "الفئة" : "Category");
        l.put("level", ar ? "المستوى" : "Level");
        l.put("priority", ar ? "الأولوية" : "Priority");
        l.put("preliminary", ar
                ? "تقرير مبدئي — الإجراء لم يكتمل بعد، وقد تتغير البيانات."
                : "Preliminary report — the procedure is not completed yet and the data may still change.");
        l.put("cancelledWatermark", ar ? "ملغى" : "CANCELLED");

        // Status
        l.put("status_REQUESTED", ar ? "مطلوب" : "Requested");
        l.put("status_STARTED", ar ? "بدأ" : "Started");
        l.put("status_CANCELLED", ar ? "ملغى" : "Cancelled");
        l.put("status_COMPLETED", ar ? "مكتمل" : "Completed");
        l.put("status_AWAITING_CONSENT", ar ? "بانتظار الموافقة" : "Awaiting Consent");
        l.put("status_IN_PROGRESS", ar ? "قيد التنفيذ" : "In Progress");
        l.put("status_SUBMITTED", ar ? "مُرسل" : "Submitted");
        l.put("status_WAITING_PRE_AUTHORIZATION", ar ? "بانتظار الموافقة المسبقة" : "Waiting Pre-Authorization");
        l.put("status_RETURNED_TO_BED", ar ? "أُعيد إلى السرير" : "Returned To Bed");

        // Allergies
        l.put("allergyAlert", ar ? "تنبيه حساسية" : "Allergy Alert");
        l.put("noKnownAllergies", ar ? "لا توجد حساسية مسجلة" : "No known allergies");

        // Timeline + KPIs
        l.put("timeline", ar ? "المسار الزمني للإجراء" : "Procedure Timeline");
        l.put("stepRequested", ar ? "الطلب" : "Requested");
        l.put("stepScheduled", ar ? "الموعد" : "Scheduled");
        l.put("stepRegistered", ar ? "التسجيل" : "Registered");
        l.put("stepAnesthesia", ar ? "التخدير" : "Anesthesia");
        l.put("stepStarted", ar ? "البدء" : "Started");
        l.put("stepEnded", ar ? "الانتهاء" : "Ended");
        l.put("stepRecovery", ar ? "الإفاقة" : "Recovery");
        l.put("stepCompleted", ar ? "الاكتمال" : "Completed");
        l.put("stepCancelled", ar ? "الإلغاء" : "Cancelled");
        l.put("kpiDuration", ar ? "مدة الإجراء" : "Procedure Duration");
        l.put("kpiAnesthesia", ar ? "مدة التخدير" : "Anesthesia Duration");
        l.put("kpiRequestToStart", ar ? "من الطلب حتى البدء" : "Request → Start");
        l.put("kpiAldrete", ar ? "مقياس ألدريت" : "Aldrete Score");
        l.put("kpiComplications", ar ? "المضاعفات" : "Complications");
        l.put("kpiSevere", ar ? "شديدة" : "severe");
        l.put("kpiTeam", ar ? "الفريق الحاضر" : "Team Present");

        // Sections
        l.put("secPatient", ar ? "المريض والزيارة" : "Patient & Encounter");
        l.put("secRequest", ar ? "تفاصيل الطلب" : "Request Details");
        l.put("secRegistration", ar ? "التسجيل والموافقة" : "Registration & Consent");
        l.put("secTeam", ar ? "فريق الإجراء" : "Procedure Team");
        l.put("secPre", ar ? "التقييم قبل الإجراء" : "Pre-Procedure Assessment");
        l.put("secIntra", ar ? "تنفيذ الإجراء" : "Intra-Procedure");
        l.put("secMonitoring", ar ? "مراقبة العلامات الحيوية" : "Vital Signs Monitoring");
        l.put("secVitalsJourney", ar ? "العلامات الحيوية عبر المراحل" : "Vitals Across Phases");
        l.put("secMedications", ar ? "الأدوية المعطاة" : "Medications Administered");
        l.put("secComplications", ar ? "المضاعفات" : "Complications");
        l.put("secOutcome", ar ? "النتيجة والملاحظات" : "Outcome & Findings");
        l.put("secPost", ar ? "الرعاية بعد الإجراء" : "Post-Procedure Care");
        l.put("secEquipment", ar ? "المعدات والمستلزمات" : "Equipment & Consumables");
        l.put("secHome", ar ? "تعليمات المنزل" : "Home Instructions");
        l.put("secCancellation", ar ? "بيانات الإلغاء" : "Cancellation");
        l.put("secSignatures", ar ? "التواقيع" : "Signatures");

        // Patient
        l.put("patientName", ar ? "اسم المريض" : "Patient Name");
        l.put("mrn", ar ? "رقم الملف" : "MRN");
        l.put("gender", ar ? "الجنس" : "Gender");
        l.put("dobAge", ar ? "تاريخ الميلاد / العمر" : "DOB / Age");
        l.put("encounterNumber", ar ? "رقم الزيارة" : "Encounter No.");
        l.put("encounterDate", ar ? "تاريخ الزيارة" : "Encounter Date");
        l.put("encounterType", ar ? "نوع الزيارة" : "Encounter Type");
        l.put("encounterDepartment", ar ? "قسم الزيارة" : "Encounter Department");
        l.put("attendingPractitioner", ar ? "الطبيب المعالج" : "Attending Practitioner");

        // Request
        l.put("requestedBy", ar ? "طُلب بواسطة" : "Requested By");
        l.put("requestedAt", ar ? "تاريخ الطلب" : "Requested At");
        l.put("fromDepartment", ar ? "القسم الطالب" : "Requesting Department");
        l.put("toFacility", ar ? "المنشأة المنفذة" : "Target Facility");
        l.put("toDepartment", ar ? "القسم المنفذ" : "Target Department");
        l.put("scheduledAt", ar ? "الموعد المحدد" : "Scheduled At");
        l.put("indication", ar ? "دواعي الإجراء" : "Indication");
        l.put("bodyPart", ar ? "جزء الجسم" : "Body Part");
        l.put("side", ar ? "الجهة" : "Side");
        l.put("notes", ar ? "ملاحظات" : "Notes");
        l.put("extraDocumentation", ar ? "توثيق إضافي" : "Extra Documentation");

        // Registration
        l.put("consentForm", ar ? "نموذج الموافقة" : "Consent Form");
        l.put("registeredAt", ar ? "تاريخ التسجيل" : "Registered At");
        l.put("location", ar ? "مكان الإجراء" : "Procedure Location");
        l.put("performingPractitioner", ar ? "الطبيب المنفذ" : "Performing Practitioner");
        l.put("notRecorded", ar ? "لم يتم التوثيق" : "Not recorded");

        // Team
        l.put("staffName", ar ? "الاسم" : "Name");
        l.put("jobRole", ar ? "الوظيفة" : "Job Role");
        l.put("responsibility", ar ? "المسؤولية" : "Responsibility");
        l.put("presentAtTimeOut", ar ? "حاضر في وقفة التحقق" : "Present at Time-Out");
        l.put("noStaff", ar ? "لم يُسجل فريق للإجراء." : "No team members recorded.");

        // Pre
        l.put("preparation", ar ? "التحضير" : "Preparation");
        l.put("fastingRequired", ar ? "الصيام مطلوب" : "Fasting Required");
        l.put("patientPrepared", ar ? "المريض جاهز" : "Patient Prepared");
        l.put("specialInstructions", ar ? "تعليمات خاصة" : "Special Instructions");
        l.put("safetyChecklist", ar ? "قائمة التحقق من السلامة" : "Safety Checklist");
        l.put("chkIdentityVerified", ar ? "تم التحقق من هوية المريض" : "Patient identity verified");
        l.put("chkConsentConfirmed", ar ? "تم تأكيد الموافقة" : "Consent confirmed");
        l.put("chkSiteMarked", ar ? "تم تحديد موضع الإجراء" : "Procedure site marked");
        l.put("chkAllergiesConfirmed", ar ? "تم تأكيد الحساسية" : "Allergies confirmed");
        l.put("chkPremedicated", ar ? "تم إعطاء الأدوية المسبقة" : "Patient premedicated");
        l.put("chkEquipmentCounted", ar ? "تم عدّ الأدوات" : "Equipment counting done");
        l.put("preVitals", ar ? "العلامات الحيوية قبل الإجراء" : "Pre-Procedure Vitals");
        l.put("preAnesthesia", ar ? "تقييم ما قبل التخدير" : "Pre-Anesthesia");
        l.put("asaScore", ar ? "تصنيف ASA" : "ASA Score");
        l.put("airwayGrade", ar ? "درجة مجرى الهواء" : "Airway Grade");
        l.put("yes", ar ? "نعم" : "Yes");
        l.put("no", ar ? "لا" : "No");

        // Vitals
        l.put("bloodPressure", ar ? "ضغط الدم" : "Blood Pressure");
        l.put("heartRate", ar ? "النبض" : "Heart Rate");
        l.put("temperature", ar ? "الحرارة" : "Temperature");
        l.put("oxygenSaturation", ar ? "تشبع الأكسجين" : "SpO₂");
        l.put("respiratoryRate", ar ? "معدل التنفس" : "Resp. Rate");
        l.put("etco2", "EtCO₂");
        l.put("fluidsGiven", ar ? "السوائل المعطاة" : "Fluids Given");
        l.put("urineOutput", ar ? "إخراج البول" : "Urine Output");
        l.put("time", ar ? "الوقت" : "Time");
        l.put("unitBp", "mmHg");
        l.put("unitHr", ar ? "نبضة/د" : "bpm");
        l.put("unitTemp", "°C");
        l.put("unitSpo2", "%");
        l.put("unitRr", ar ? "نفس/د" : "/min");
        l.put("unitMl", ar ? "مل" : "mL");
        l.put("phasePre", ar ? "قبل" : "Pre");
        l.put("phaseIntra", ar ? "أثناء (آخر قراءة)" : "Intra (last)");
        l.put("phasePost", ar ? "بعد" : "Post");
        l.put("trend", ar ? "الاتجاه" : "Trend");
        l.put("hrRange", ar ? "مدى النبض" : "HR range");
        l.put("spo2Lowest", ar ? "أدنى تشبع" : "Lowest SpO₂");
        l.put("sbpRange", ar ? "مدى الضغط الانقباضي" : "Systolic range");
        l.put("totalFluids", ar ? "إجمالي السوائل" : "Total fluids");
        l.put("totalUrine", ar ? "إجمالي البول" : "Total urine");
        l.put("legendSbpDbp", ar ? "الضغط الانقباضي / الانبساطي" : "Systolic / Diastolic");
        l.put("noVitals", ar ? "لا توجد قراءات مسجلة أثناء الإجراء." : "No readings recorded during the procedure.");

        // Intra
        l.put("procedurePeriod", ar ? "فترة الإجراء" : "Procedure Period");
        l.put("actualStart", ar ? "البدء الفعلي" : "Actual Start");
        l.put("actualEnd", ar ? "الانتهاء الفعلي" : "Actual End");
        l.put("duration", ar ? "المدة" : "Duration");
        l.put("anesthesia", ar ? "التخدير" : "Anesthesia");
        l.put("anesthesiaType", ar ? "نوع التخدير" : "Type");
        l.put("anesthesiaStart", ar ? "بداية التخدير" : "Start");
        l.put("anesthesiaEnd", ar ? "نهاية التخدير" : "End");
        l.put("administeredBy", ar ? "أُعطي بواسطة" : "Administered By");
        l.put("noAnesthesia", ar ? "لم يُستخدم تخدير" : "No anesthesia used");
        l.put("teamTimeOut", ar ? "وقفة التحقق للفريق" : "Team Time-Out");
        l.put("timeOutDone", ar ? "تمت وقفة التحقق قبل البدء" : "Time-out performed before incision");
        l.put("timeOutNotDone", ar ? "لم تُسجل وقفة التحقق" : "Time-out not recorded");

        // Medications
        l.put("medication", ar ? "الدواء (المادة الفعالة)" : "Medication (Active Ingredient)");
        l.put("dose", ar ? "الجرعة" : "Dose");
        l.put("givenAt", ar ? "وقت الإعطاء" : "Given At");
        l.put("givenBy", ar ? "بواسطة" : "By");

        // Complications
        l.put("complicationType", ar ? "نوع المضاعفة" : "Complication");
        l.put("severity", ar ? "الشدة" : "Severity");
        l.put("actionsTaken", ar ? "الإجراءات المتخذة" : "Actions Taken");
        l.put("noComplications", ar ? "لم تُسجل أي مضاعفات" : "No complications recorded");

        // Outcome
        l.put("outcome", ar ? "نتيجة الإجراء" : "Outcome");
        l.put("observations", ar ? "الملاحظات" : "Observations");
        l.put("result", ar ? "النتيجة / التقرير" : "Result / Findings");
        l.put("additionalNotes", ar ? "ملاحظات إضافية" : "Additional Notes");

        // Post care
        l.put("postDiagnoses", ar ? "التشخيص بعد الإجراء" : "Post-Procedure Diagnosis");
        l.put("patientCondition", ar ? "حالة المريض" : "Patient Condition");
        l.put("postVitals", ar ? "العلامات الحيوية بعد الإجراء" : "Post-Procedure Vitals");
        l.put("pain", ar ? "تقييم الألم" : "Pain Assessment");
        l.put("painTool", ar ? "أداة التقييم" : "Tool");
        l.put("painScore", ar ? "درجة الألم" : "Score");
        l.put("painDescription", ar ? "وصف الألم" : "Description");
        l.put("aldrete", ar ? "مقياس ألدريت للإفاقة" : "Aldrete Recovery Score");
        l.put("aldActivity", ar ? "النشاط" : "Activity");
        l.put("aldRespiration", ar ? "التنفس" : "Respiration");
        l.put("aldCirculation", ar ? "الدورة الدموية" : "Circulation");
        l.put("aldConsciousness", ar ? "الوعي" : "Consciousness");
        l.put("aldOxygen", ar ? "تشبع الأكسجين" : "O₂ Saturation");
        l.put("aldReady", ar ? "يحقق حد الخروج من الإفاقة (≥ 9)" : "Meets recovery discharge threshold (≥ 9)");
        l.put("aldNotReady", ar ? "دون حد الخروج من الإفاقة (≥ 9)" : "Below recovery discharge threshold (≥ 9)");
        l.put("points", ar ? "نقاط" : "pts");
        l.put("dischargeReadiness", ar ? "جاهزية الخروج" : "Discharge Readiness");
        l.put("criteriaMet", ar ? "معايير متحققة" : "criteria met");
        l.put("dcAwake", ar ? "واعٍ ومدرك" : "Awake and oriented");
        l.put("dcOralFluids", ar ? "يتحمل السوائل الفموية" : "Tolerating oral fluids");
        l.put("dcAmbulating", ar ? "يمشي باستقلالية" : "Ambulating independently");
        l.put("dcVoided", ar ? "تبوّل" : "Voided urine");
        l.put("dcNoBleeding", ar ? "لا يوجد نزيف نشط" : "No active bleeding");
        l.put("dcPainControlled", ar ? "درجة الألم ≤ 4" : "Pain score ≤ 4");
        l.put("nauseaVomiting", ar ? "غثيان / تقيؤ" : "Nausea / Vomiting");
        l.put("equipmentCount", ar ? "عدّ الأدوات بعد الإجراء" : "Post-Procedure Equipment Count");
        l.put("countStatus", ar ? "حالة العدّ" : "Count Status");
        l.put("recoveryNotes", ar ? "ملاحظات الإفاقة" : "Recovery Notes");
        l.put("additionalObservations", ar ? "ملاحظات إضافية" : "Additional Observations");
        l.put("recordedAtBy", ar ? "وُثّق في / بواسطة" : "Recorded At / By");

        // Equipment
        l.put("item", ar ? "الصنف" : "Item");
        l.put("type", ar ? "النوع" : "Type");
        l.put("quantity", ar ? "الكمية" : "Qty");

        // Home + cancellation
        l.put("homeInstruction", ar ? "التعليمات" : "Instruction");
        l.put("homeInstructionNotes", ar ? "تفاصيل التعليمات" : "Details");
        l.put("cancelledAt", ar ? "تاريخ الإلغاء" : "Cancelled At");
        l.put("cancelledBy", ar ? "أُلغي بواسطة" : "Cancelled By");
        l.put("cancellationReason", ar ? "سبب الإلغاء" : "Reason");

        // Signatures
        l.put("sigPerforming", ar ? "الطبيب المنفذ" : "Performing Practitioner");
        l.put("sigAnesthesia", ar ? "طبيب التخدير" : "Anesthesia");
        l.put("sigRecordedBy", ar ? "وثّق بواسطة" : "Documented By");
        l.put("signature", ar ? "التوقيع" : "Signature");
        l.put("verifyTitle", ar ? "تحقق من صحة التقرير" : "Verify this report");
        l.put("verifyHint", ar
                ? "امسح الرمز للتأكد أن التقرير صادر من النظام وقارن بياناته."
                : "Scan to confirm this report was issued by the system and compare its details.");
        l.put("disclaimer", ar
                ? "هذا التقرير صادر من النظام بناءً على البيانات الموثقة في سير عمل الإجراء."
                : "This report is generated from the data documented in the procedure workflow.");
        return l;
    }
}
