package com.dazzle.asklepios.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportVerificationServiceTest {

    private static final String JWT_SECRET = Base64.getEncoder().encodeToString("a-test-secret-that-is-long-enough-1234567890".getBytes());
    private final ReportVerificationService service = new ReportVerificationService("", JWT_SECRET);
    private final Instant issued = Instant.parse("2026-11-12T10:00:00Z");

    @Test
    void signedToken_verifiesBackToItsProcedureAndTime() {
        String token = service.sign(5L, issued);

        var verified = service.verify(token).orElseThrow();

        assertEquals(5L, verified.procedureRequestId());
        assertEquals(issued, verified.issuedAt());
    }

    @Test
    void tamperedTokens_areRejected() {
        String token = service.sign(5L, issued);
        String[] parts = token.split("\\.");

        assertTrue(service.verify(parts[0].replace("5", "6") + "." + parts[1] + "." + parts[2]).isEmpty(), "changed id");
        assertTrue(service.verify(parts[0] + "." + (Long.parseLong(parts[1]) + 1) + "." + parts[2]).isEmpty(), "changed time");
        assertTrue(service.verify(parts[0] + "." + parts[1] + ".AAAAAAAAAAAAAAAAAAAAAA").isEmpty(), "forged signature");
        assertTrue(service.verify("5.123").isEmpty());
        assertTrue(service.verify("abc.def.ghi").isEmpty());
        assertTrue(service.verify(null).isEmpty());
    }

    @Test
    void tokensFromAnotherSecret_areRejected() {
        ReportVerificationService other = new ReportVerificationService("different", JWT_SECRET);

        assertTrue(service.verify(other.sign(5L, issued)).isEmpty());
    }

    @Test
    void verificationUrl_usesOnlyTheOriginOfAnHttpBase() {
        assertEquals("https://his.example.com/#/verify-report?t=1.2.x",
                service.verificationUrl("https://his.example.com/some/path?x=1", "1.2.x"));
        assertEquals("http://localhost:3100/#/verify-report?t=1.2.x",
                service.verificationUrl("http://localhost:3100", "1.2.x"));
        assertNull(service.verificationUrl("javascript:alert(1)", "1.2.x"));
        assertNull(service.verificationUrl("", "1.2.x"));
        assertNull(service.verificationUrl(null, "1.2.x"));
    }

    @Test
    void qrSvg_isAScalableSvg() {
        String svg = service.qrSvg("https://his.example.com/#/verify-report?t=1.2.x");

        assertTrue(svg.startsWith("<svg"));
        assertTrue(svg.contains("crispEdges"));
        assertTrue(svg.contains("h1v1h-1z"));
    }

    @Test
    void qrSvg_decodesBackToTheLink() throws Exception {
        String url = service.verificationUrl("https://his.example.com", service.sign(5L, issued));
        String svg = service.qrSvg(url);

        // Paint the SVG's unit squares onto an image and let ZXing read it like a scanner would
        java.util.regex.Matcher size = java.util.regex.Pattern.compile("viewBox=\"0 0 (\\d+) (\\d+)\"").matcher(svg);
        assertTrue(size.find());
        int modules = Integer.parseInt(size.group(1));
        int scale = 8;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                modules * scale, modules * scale, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(java.awt.Color.BLACK);
        java.util.regex.Matcher cell = java.util.regex.Pattern.compile("M(\\d+) (\\d+)h1v1h-1z").matcher(svg);
        while (cell.find()) {
            g.fillRect(Integer.parseInt(cell.group(1)) * scale, Integer.parseInt(cell.group(2)) * scale, scale, scale);
        }
        g.dispose();

        String decoded = new com.google.zxing.qrcode.QRCodeReader().decode(new com.google.zxing.BinaryBitmap(
                new com.google.zxing.common.HybridBinarizer(
                        new com.google.zxing.client.j2se.BufferedImageLuminanceSource(image)))).getText();

        assertEquals(url, decoded);
    }

    @Test
    void initialsAndMaskedMrn_hideTheFullIdentity() {
        assertEquals("M. N. Z.", ProcedureReportVerificationService.initials("masa", "nabeel", null, "zahran"));
        assertNull(ProcedureReportVerificationService.initials(null, " "));
        assertEquals("P1***8", ProcedureReportVerificationService.maskMrn("P14738"));
        assertEquals("P**", ProcedureReportVerificationService.maskMrn("P14"));
        assertNull(ProcedureReportVerificationService.maskMrn(null));
    }
}
