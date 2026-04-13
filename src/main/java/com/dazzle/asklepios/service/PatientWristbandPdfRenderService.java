package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.patient.PatientWristbandDTO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PatientWristbandPdfRenderService {

    private final SpringTemplateEngine templateEngine;
    private final PatientService patientService;

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateWristbandPdf(Long patientId) {
        PatientWristbandDTO dto = patientService.getPatientWristband(patientId);

        String dateOfBirthFormatted = dto.dateOfBirth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(dto.dateOfBirth())
                : "—";

        String admissionDateTimeFormatted = dto.admissionDateTime() != null
                ? dto.admissionDateTime().format(DATE_TIME_FORMAT)
                : "—";

        String qrValue = buildQrValue(dto, dateOfBirthFormatted, admissionDateTimeFormatted);

        Context context = new Context();
        context.setVariable("wristband", dto);
        context.setVariable("dateOfBirthFormatted", dateOfBirthFormatted);
        context.setVariable("admissionDateTimeFormatted", admissionDateTimeFormatted);
        context.setVariable("qrImage", generateQrBase64(qrValue, 220, 220));
        context.setVariable("barcodeImage", generateCode128BarcodeBase64(dto.mrn(), 520, 110));

        String html = templateEngine.process("reports/patient-wristband", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ConverterProperties props = new ConverterProperties();
        HtmlConverter.convertToPdf(html, outputStream, props);

        return outputStream.toByteArray();
    }

    private String buildQrValue(PatientWristbandDTO dto,
                                String dateOfBirthFormatted,
                                String admissionDateTimeFormatted) {
        return "MRN:" + nullSafe(dto.mrn())
                + ";NAME:" + nullSafe(dto.fullName())
                + ";DOB:" + nullSafe(dateOfBirthFormatted)
                + ";GENDER:" + nullSafe(dto.gender())
                + ";BLOOD_GROUP:" + nullSafe(dto.bloodGroup())
                + ";ALLERGY:" + nullSafe(dto.allergyAlert())
                + ";ADMISSION_DT:" + nullSafe(admissionDateTimeFormatted)
                + ";FACILITY:" + nullSafe(dto.facilityName());
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