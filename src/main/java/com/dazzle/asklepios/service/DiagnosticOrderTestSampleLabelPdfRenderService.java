package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.DiagnosticOrderTestSampleLabelDTO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosticOrderTestSampleLabelPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final DiagnosticOrderTestCollectedSampleService sampleService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateSampleLabelPdf(Long orderTestId) {
        DiagnosticOrderTestSampleLabelDTO dto = sampleService.getSampleLabel(orderTestId);

        String sampleDateTime = dto.sampleDateTime() != null
                ? DATE_TIME_FORMAT.format(dto.sampleDateTime().atZone(ZoneId.systemDefault()))
                : "—";

        String today = DATE_FORMAT.format(java.time.LocalDate.now());

        String qrValue = buildQrValue(dto, sampleDateTime);

        Context context = new Context();
        context.setVariable("label", dto);
        context.setVariable("today", today);
        context.setVariable("sampleDateTimeFormatted", sampleDateTime);
        context.setVariable("sampleQuantityFormatted", formatQuantity(dto.sampleQuantity()));
        context.setVariable("expiryDate", dto.expiryDate());
        context.setVariable("qrImage", generateQrBase64(qrValue, 220, 220));
        context.setVariable("barcodeImage", generateCode128BarcodeBase64(dto.mrn(), 520, 110));

        String html = templateEngine.process("reports/sample-label", context);

        return renderPdfWithChromium(html);
    }

    private byte[] renderPdfWithChromium(String html) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            BrowserContext browserContext = browser.newContext();
            Page page = browserContext.newPage();

            page.setContent(
                    html,
                    new Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE)
            );

            byte[] pdfBytes = page.pdf(
                    new Page.PdfOptions()
                            .setPrintBackground(true)
                            .setPreferCSSPageSize(true)
            );

            browserContext.close();
            browser.close();

            return pdfBytes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate sample label PDF with Chromium: " + e.getMessage(), e);
        }
    }

    private String buildQrValue(DiagnosticOrderTestSampleLabelDTO dto, String sampleDateTime) {
        return "MRN:" + nullSafe(dto.mrn())
                + ";NAME:" + nullSafe(dto.patientName())
                + ";TEST:" + nullSafe(dto.testName())
                + ";SAMPLE_DT:" + nullSafe(sampleDateTime)
                + ";SOURCE:" + nullSafe(dto.sourceOfSample())
                + ";EXPIRY:" + (dto.expiryDate() != null
                ? DATE_TIME_FORMAT.format(dto.expiryDate().atZone(ZoneId.systemDefault()))
                : "—")
                + ";QTY:" + formatQuantity(dto.sampleQuantity()) + " " + nullSafe(dto.sampleUnit());
    }

    private String formatQuantity(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String generateQrBase64(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );

            BufferedImage image = toBufferedImage(bitMatrix);
            return toBase64Png(image);
        } catch (Exception e) {
            return "";
        }
    }

    private String generateCode128BarcodeBase64(String content, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    nullSafe(content),
                    BarcodeFormat.CODE_128,
                    width,
                    height
            );

            BufferedImage image = toBufferedImage(bitMatrix);
            return toBase64Png(image);
        } catch (Exception e) {
            return "";
        }
    }

    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(Color.BLACK);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }

        graphics.dispose();

        return image;
    }

    private String toBase64Png(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }
}