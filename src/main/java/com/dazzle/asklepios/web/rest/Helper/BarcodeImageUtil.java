package com.dazzle.asklepios.web.rest.Helper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class BarcodeImageUtil {

    public static String generateQrBase64(String content, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );

            BufferedImage image = toBufferedImage(bitMatrix);
            return toBase64(image);
        } catch (Exception e) {
            return "";
        }
    }

    public static String generateCode128BarcodeBase64(String content, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.CODE_128,
                    width,
                    height
            );

            BufferedImage image = toBufferedImage(bitMatrix);
            return toBase64(image);
        } catch (Exception e) {
            return "";
        }
    }

    private static BufferedImage toBufferedImage(BitMatrix matrix) {
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

    private static String toBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }
}
