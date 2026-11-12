package com.dazzle.asklepios.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Signs and checks the token printed as a QR code on reports, so anyone holding a paper copy can
 * confirm it was issued by this system.
 *
 * <p>Token: {@code <procedureRequestId>.<issuedAtEpochSeconds>.<signature>}, where the signature is
 * the first 16 bytes of HMAC-SHA256 over {@code procedure-report:<id>:<issuedAt>}, base64url encoded.
 * The HMAC key is derived from {@code report.verification-secret}, or from the JWT secret when that
 * is not set, with a fixed label so the JWT key itself never signs report tokens.
 */
@Service
public class ReportVerificationService {

    public static final String VERIFY_ROUTE = "/#/verify-report";

    private static final String PURPOSE = "procedure-report";
    private static final int SIGNATURE_BYTES = 16;
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_DECODER = Base64.getUrlDecoder();

    private final byte[] key;

    public ReportVerificationService(
            @Value("${report.verification-secret:}") String verificationSecret,
            @Value("${analytics.security.authentication.jwt.base64-secret:}") String jwtSecret
    ) {
        byte[] root = !verificationSecret.isBlank()
                ? verificationSecret.getBytes(StandardCharsets.UTF_8)
                : decodeOrRaw(jwtSecret);
        if (root.length == 0) {
            throw new IllegalStateException("No secret configured for report verification");
        }
        this.key = hmac(root, "report-verification-v1".getBytes(StandardCharsets.UTF_8));
    }

    public record Verified(Long procedureRequestId, Instant issuedAt) {
    }

    public String sign(Long procedureRequestId, Instant issuedAt) {
        long issued = issuedAt.getEpochSecond();
        return procedureRequestId + "." + issued + "." + signature(procedureRequestId, issued);
    }

    /**
     * @return the procedure request and issue time when the token is well formed and its signature
     * matches, otherwise empty
     */
    public Optional<Verified> verify(String token) {
        if (token == null || token.length() > 200) {
            return Optional.empty();
        }
        String[] parts = token.trim().split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            long id = Long.parseLong(parts[0]);
            long issued = Long.parseLong(parts[1]);
            byte[] expected = B64_DECODER.decode(signature(id, issued));
            byte[] actual = B64_DECODER.decode(parts[2]);
            if (id <= 0 || !MessageDigest.isEqual(expected, actual)) {
                return Optional.empty();
            }
            return Optional.of(new Verified(id, Instant.ofEpochSecond(issued)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Builds the public verification link from the UI origin the report was printed from.
     *
     * @return the link, or null when the origin is missing or not an http(s) URL
     */
    public String verificationUrl(String uiBaseUrl, String token) {
        if (uiBaseUrl == null || uiBaseUrl.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(uiBaseUrl.trim());
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null) {
                return null;
            }
            String origin = uri.getScheme() + "://" + uri.getRawAuthority();
            return origin + VERIFY_ROUTE + "?t=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Draws the QR code as SVG (one path of unit squares) so it stays sharp in the PDF.
     */
    public String qrSvg(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.CHARACTER_SET, "UTF-8"
            ));
            int w = matrix.getWidth();
            int h = matrix.getHeight();
            StringBuilder path = new StringBuilder();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (matrix.get(x, y)) {
                        path.append('M').append(x).append(' ').append(y).append("h1v1h-1z");
                    }
                }
            }
            return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 " + w + " " + h
                    + "\" shape-rendering=\"crispEdges\"><rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>"
                    + "<path fill=\"#0f172a\" d=\"" + path + "\"/></svg>";
        } catch (Exception e) {
            return null;
        }
    }

    private String signature(long id, long issued) {
        byte[] mac = hmac(key, (PURPOSE + ":" + id + ":" + issued).getBytes(StandardCharsets.UTF_8));
        return B64.encodeToString(Arrays.copyOf(mac, SIGNATURE_BYTES));
    }

    private static byte[] hmac(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("HmacSHA256 unavailable", e);
        }
    }

    private static byte[] decodeOrRaw(String secret) {
        if (secret == null || secret.isBlank()) {
            return new byte[0];
        }
        try {
            return Base64.getDecoder().decode(secret.trim());
        } catch (IllegalArgumentException e) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
