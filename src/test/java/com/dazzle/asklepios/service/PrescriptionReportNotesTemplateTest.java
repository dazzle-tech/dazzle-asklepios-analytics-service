package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.prescription.PrescriptionMedicationDTO;
import com.dazzle.asklepios.service.dto.prescription.PrescriptionPrintDTO;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrescriptionReportNotesTemplateTest {

    private SpringTemplateEngine templateEngine;
    private String reportCss;

    @BeforeEach
    void setUp() throws Exception {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCheckExistence(true);
        resolver.setCacheable(false);

        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        reportCss = Files.readString(
                Path.of("src/main/resources/templates/reports/styles/prescription-report.css")
                        .toAbsolutePath()
        ).replace("__PRIMARY_COLOR__", "#1976d2");
    }

    @Test
    void englishHtml_associatesNotesWithCorrectMedication_andHidesEmptyWhitespace() {
        String html = render(false);

        assertTrue(html.contains("medication-notes-section"));
        assertFalse(html.contains("med-details-row"), "Notes must not remain inside the medications table");
        assertTrue(html.contains("3. Medication Notes &amp; Documentation")
                || html.contains("3. Medication Notes & Documentation"));
        assertTrue(html.contains("4. Clinical Observations"));
        assertFalse(html.contains(">Refills</th>"),
                "Refills column should be removed from medications table");
        assertTrue(html.contains("med-notes-card"));
        assertTrue(html.contains("Parameters to Monitor"));
        assertTrue(html.contains("BP, glucose"));
        assertTrue(html.contains(">0</span>") || html.contains(">0</"), "Refill count 0 should remain visible");

        int notesSectionIdx = html.indexOf("medication-notes-section");
        int alphaInNotesIdx = html.indexOf("Med Alpha", notesSectionIdx);
        int betaInNotesIdx = html.indexOf("Med Beta", notesSectionIdx);
        int alphaNotesIdx = html.indexOf("Take with glass of water\nafter breakfast");
        int betaExtraIdx = html.indexOf("Extra docs for Beta only");
        int diagnosesIdx = html.indexOf("Clinical Observations");

        assertTrue(notesSectionIdx > html.indexOf("medication-table"),
                "Notes section should come after the medications table");
        assertTrue(alphaInNotesIdx > notesSectionIdx && alphaNotesIdx > alphaInNotesIdx
                        && alphaNotesIdx < betaInNotesIdx,
                "Alpha notes should be grouped under Med Alpha in the notes section");
        assertTrue(betaExtraIdx > betaInNotesIdx && betaExtraIdx < diagnosesIdx,
                "Beta extra documentation should be grouped under Med Beta in the notes section");

        String notesSectionHtml = html.substring(notesSectionIdx, diagnosesIdx);
        assertEquals(2, notesSectionHtml.split("class=\"med-notes-card\"", -1).length - 1,
                "Only medications with real notes/docs/refills should appear in the notes section");
        assertFalse(notesSectionHtml.contains(">Med Gamma<"),
                "Gamma has only whitespace/empty fields and should not appear in notes section");

        assertTrue(html.contains(">Notes<") || html.contains(">Notes</"));
        assertTrue(html.contains(">Extra Documentation<") || html.contains(">Extra Documentation</"));
        assertTrue(html.contains("&lt;script&gt;x&lt;/script&gt;"), "th:text should escape HTML in notes");
        assertTrue(html.contains("&lt;b&gt;escaped&lt;/b&gt;"), "HTML in extra documentation should be escaped");
        assertFalse(html.contains("<script>x</script>"), "Raw script tags must not appear unescaped");
        assertTrue(html.contains("med-notes-value"));
        assertTrue(reportCss.contains("white-space: pre-wrap"));
    }

    @Test
    void englishHtml_hidesNotesSectionWhenAllFieldsEmpty() {
        PrescriptionPrintDTO emptyNotesReport = sampleReportWithNoNotes();
        String html = render(false, emptyNotesReport);
        assertFalse(html.contains("medication-notes-section"));
        assertTrue(html.contains("3. Clinical Observations"));
        assertTrue(html.contains("Med Alpha"));
    }

    @Test
    void arabicHtml_usesArabicLabels_andRtlDirection() {
        String html = render(true);

        assertTrue(html.contains("dir=\"rtl\"") || html.contains("dir='rtl'") || html.contains("class=\"rtl\""));
        assertTrue(html.contains("ملاحظات وتوثيق الأدوية"));
        assertTrue(html.contains("ملاحظات"));
        assertTrue(html.contains("توثيق إضافي"));
        assertTrue(html.contains("المعاملات للمراقبة") || html.contains("إعادات الصرف"));
        assertFalse(html.contains(">Notes</"));
        assertFalse(html.contains(">Extra Documentation</"));
    }

    @Test
    void pdfGeneration_keepsNotesWithMedications_andDoesNotOverlapFooter() throws Exception {
        String html = render(false);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );
            Page page = browser.newPage();
            page.setContent(html);

            byte[] pdf = page.pdf(new Page.PdfOptions()
                    .setFormat("A4")
                    .setPrintBackground(true)
                    .setPreferCSSPageSize(true));

            assertTrue(pdf.length > 1000, "PDF should be generated with content");

            Path outDir = Path.of("build/tmp");
            Files.createDirectories(outDir);
            Files.write(outDir.resolve("prescription-notes-verification.pdf"), pdf);

            page.setViewportSize(900, 1400);
            byte[] sectionShot = page.locator(".medication-notes-section").first().screenshot();
            Files.write(outDir.resolve("prescription-notes-section.png"), sectionShot);
            byte[] pageShot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            Files.write(outDir.resolve("prescription-notes-fullpage.png"), pageShot);

            String bodyText = page.innerText("body");
            int notesSectionPos = indexOfIgnoreCase(bodyText, "Medication Notes");
            int alphaInNotesPos = indexOfIgnoreCase(bodyText.substring(notesSectionPos), "Med Alpha") + notesSectionPos;
            int alphaNotesPos = bodyText.indexOf("Take with glass of water");
            int betaInNotesPos = indexOfIgnoreCase(bodyText.substring(notesSectionPos), "Med Beta") + notesSectionPos;
            int betaExtraPos = bodyText.indexOf("Extra docs for Beta only");
            int footerPos = indexOfIgnoreCase(bodyText, "Prescriber Signature");

            assertTrue(notesSectionPos >= 0, "Notes section title should be present");
            assertTrue(alphaNotesPos > alphaInNotesPos && alphaNotesPos < betaInNotesPos,
                    "Alpha notes should stay under Med Alpha in the notes section");
            assertTrue(betaExtraPos > betaInNotesPos, "Beta extra documentation should stay under Med Beta");
            assertTrue(footerPos > betaExtraPos, "Footer should appear after medication notes");
            assertTrue(bodyText.contains("BP, glucose"));
            assertTrue(indexOfIgnoreCase(bodyText, "Parameters to Monitor") >= 0);

            var lastCard = page.locator(".med-notes-card").last().boundingBox();
            var footer = page.locator(".footer").first().boundingBox();
            assertTrue(lastCard != null && footer != null, "Card and footer boxes should be measurable");
            assertTrue(footer.y >= lastCard.y + lastCard.height - 1,
                    "Footer must not overlap medication notes cards");
            assertTrue(page.locator(".med-notes-card").count() >= 2);
            assertTrue(page.locator(".med-notes-card-header").count() >= 2);
            assertTrue(page.locator(".med-details-row").count() == 0,
                    "Medications table must not contain notes detail rows");

            browser.close();
        }
    }

    private static int indexOfIgnoreCase(String haystack, String needle) {
        return haystack.toLowerCase().indexOf(needle.toLowerCase());
    }

    private String render(boolean arabic) {
        return render(arabic, sampleReport());
    }

    private String render(boolean arabic, PrescriptionPrintDTO report) {
        List<PrescriptionMedicationDTO> medicationsWithNotes =
                PrescriptionPdfRenderService.filterMedicationsWithNotes(report);

        Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("medicationsWithNotes", medicationsWithNotes);
        context.setVariable("lang", arabic ? "ar" : "en");
        context.setVariable("dir", arabic ? "rtl" : "ltr");
        context.setVariable("labels", labels(arabic, !medicationsWithNotes.isEmpty()));
        context.setVariable("logo", "");
        context.setVariable("stamp", "");
        context.setVariable("reportCss", reportCss);
        return templateEngine.process("reports/prescription-report", context);
    }

    private static Map<String, String> labels(boolean isArabic, boolean includeMedicationNotesSection) {
        Map<String, String> labels = new HashMap<>();
        labels.put("prescriptionReport", isArabic ? "تقرير الوصفة الطبية" : "PRESCRIPTION REPORT");
        labels.put("docId", isArabic ? "رقم المستند" : "DOC ID");
        labels.put("issueDate", isArabic ? "تاريخ الإصدار" : "Issue Date");
        labels.put("time", isArabic ? "الوقت" : "Time");
        labels.put("patientIdentification", isArabic ? "1. بيانات المريض" : "1. Patient Identification");
        labels.put("fullName", isArabic ? "الاسم الكامل" : "Full Name");
        labels.put("gender", isArabic ? "الجنس" : "Gender");
        labels.put("mrnNumber", isArabic ? "رقم الملف" : "MRN Number");
        labels.put("dobAge", isArabic ? "تاريخ الميلاد / العمر" : "D.O.B / Age");
        labels.put("encounterDepartment", isArabic ? "قسم الزيارة" : "Encounter Department");
        labels.put("encounterId", isArabic ? "رقم الزيارة" : "Encounter ID");
        labels.put("encounterDate", isArabic ? "تاريخ الزيارة" : "Encounter Date");
        labels.put("phone", isArabic ? "رقم الهاتف" : "Phone");
        labels.put("email", isArabic ? "البريد الإلكتروني" : "Email");
        labels.put("primaryDocument", isArabic ? "المستند الأساسي" : "Primary Document");
        labels.put("entryDate", isArabic ? "تاريخ الإدخال" : "Entry Date");
        labels.put("allergyType", isArabic ? "نوع الحساسية" : "Allergy Type");
        labels.put("allergen", isArabic ? "مسبب الحساسية" : "Allergen");
        labels.put("severity", isArabic ? "الدرجة" : "Severity");
        labels.put("noAllergies", isArabic ? "لا توجد حساسية مسجلة." : "No allergies recorded.");
        labels.put("activeIngredient", isArabic ? "المادة الفعالة" : "Active Ingredient");
        labels.put("medications", isArabic ? "2. الأدوية" : "2. Medications");
        labels.put("medicationName", isArabic ? "اسم الدواء" : "Medication Name");
        labels.put("instructions", isArabic ? "التعليمات" : "Instructions");
        labels.put("duration", isArabic ? "المدة" : "Duration");
        labels.put("refills", isArabic ? "إعادات الصرف" : "Refills");
        labels.put("parametersToMonitor",
                isArabic ? "المعاملات للمراقبة" : "Parameters to Monitor");
        labels.put("administrationInstructions",
                isArabic ? "تعليمات الإعطاء" : "Administration Instructions");
        labels.put("indication", isArabic ? "التشخيص العلاجي" : "Indication");
        labels.put("notes", isArabic ? "ملاحظات" : "Notes");
        labels.put("extraDocumentation", isArabic ? "توثيق إضافي" : "Extra Documentation");
        labels.put("refillsAndParameters",
                isArabic ? "إعادات الصرف والمعاملات للمراقبة" : "Refills and Parameters to Monitor");
        labels.put("noMedications",
                isArabic ? "لا توجد أدوية موصوفة حالياً." : "No active prescription medications found.");

        int section = 3;
        if (includeMedicationNotesSection) {
            labels.put(
                    "medicationNotesDocumentation",
                    section++ + ". " + (isArabic ? "ملاحظات وتوثيق الأدوية" : "Medication Notes & Documentation")
            );
        } else {
            labels.put(
                    "medicationNotesDocumentation",
                    isArabic ? "ملاحظات وتوثيق الأدوية" : "Medication Notes & Documentation"
            );
        }
        labels.put(
                "clinicalObservationsDiagnoses",
                section++ + ". " + (isArabic ? "الملاحظات السريرية والتشخيصات" : "Clinical Observations & Diagnoses")
        );
        labels.put("allergies", section + ". " + (isArabic ? "الحساسية" : "Allergies"));
        labels.put("prescriberSignature", isArabic ? "توقيع الطبيب الواصف" : "Prescriber Signature");
        labels.put("digitallyAuthenticatedBy",
                isArabic ? "تم الاعتماد إلكترونياً بواسطة" : "Digitally Authenticated By");
        return labels;
    }

    private static PrescriptionMedicationDTO med(
            String activeIngredient,
            String medicationName,
            String instructions,
            Long duration,
            String durationType,
            Boolean refillAllowed,
            Long numberOfRefills,
            String administrationInstructions,
            Boolean substitutionAllowed,
            String indicationIcd,
            String notes,
            String extraDocumentation,
            String parametersToMonitor,
            String refillInterval,
            String refillsAndParameters
    ) {
        return new PrescriptionMedicationDTO(
                activeIngredient,
                medicationName,
                instructions,
                duration,
                durationType,
                refillAllowed,
                numberOfRefills,
                administrationInstructions,
                substitutionAllowed,
                indicationIcd,
                notes,
                extraDocumentation,
                parametersToMonitor,
                refillInterval,
                refillsAndParameters
        );
    }

    private static PrescriptionPrintDTO sampleReport() {
        List<PrescriptionMedicationDTO> medications = List.of(
                med(
                        "Ingredient A",
                        "Med Alpha",
                        "1 tablet daily",
                        7L,
                        "Days",
                        true,
                        1L,
                        "Take after meal",
                        false,
                        "J06.9",
                        "Take with glass of water\nafter breakfast\n<script>x</script>",
                        "   ",
                        "BP, glucose",
                        "30 Days",
                        "1\n30 Days\nBP, glucose"
                ),
                med(
                        "Ingredient B",
                        "Med Beta",
                        "2 drops",
                        3L,
                        "Days",
                        false,
                        0L,
                        null,
                        true,
                        "R05",
                        null,
                        "Extra docs for Beta only\nline 2\n<" + "b>escaped</b>",
                        null,
                        null,
                        "0"
                ),
                med(
                        "Ingredient C",
                        "Med Gamma",
                        "as needed",
                        1L,
                        "Days",
                        false,
                        null,
                        "take with glass of water",
                        false,
                        "Z00.0",
                        "   ",
                        null,
                        "   ",
                        null,
                        null
                )
        );

        return baseReport("RX-1001", medications);
    }

    private static PrescriptionPrintDTO sampleReportWithNoNotes() {
        List<PrescriptionMedicationDTO> medications = List.of(
                med(
                        "Ingredient A",
                        "Med Alpha",
                        "1 tablet daily",
                        7L,
                        "Days",
                        true,
                        null,
                        "Take after meal",
                        false,
                        "J06.9",
                        null,
                        "   ",
                        null,
                        null,
                        null
                )
        );

        return baseReport("RX-1002", medications);
    }

    private static PrescriptionPrintDTO baseReport(String number, List<PrescriptionMedicationDTO> medications) {
        return new PrescriptionPrintDTO(
                number,
                Instant.parse("2026-03-11T10:15:30Z"),
                "Dr Prescriber",
                "prescriber@example.com",
                "Dr Practitioner",
                "General Medicine",
                "Test Facility",
                "OPD",
                "Patient Name",
                "MRN001",
                "National ID",
                "123456",
                new Date(),
                "30 Years 0 Months 0 Days",
                "MALE",
                "0500000000",
                "patient@example.com",
                "ENC-9",
                LocalDate.of(2026, 3, 11),
                "Follow up",
                "OPD",
                List.of(),
                List.of(),
                List.of(),
                medications
        );
    }
}
