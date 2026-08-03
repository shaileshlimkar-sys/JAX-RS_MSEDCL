package com.msedcl.jaxrs.urlshortner.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import io.nayuki.qrcodegen.QrCode;

public class QRUtil {

    /**
     * Converts a BufferedImage into an in-memory byte array.
     */
    public static byte[] convertImageToBytes(BufferedImage img) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Write the image format as PNG into the memory stream
            ImageIO.write(img, "png", baos);
            baos.flush();
            return baos.toByteArray();
        }
    }
    
    // Helper method 'toImage' converts a Nayuki QrCode object into a renderable BufferedImage.
    public static BufferedImage toImage(QrCode qr, int scale, int border) {
        int resultSize = (qr.size + border * 2) * scale;
        BufferedImage result = new BufferedImage(resultSize, resultSize, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < resultSize; y++) {
            for (int x = 0; x < resultSize; x++) {
                boolean isBlack = qr.getModule(x / scale - border, y / scale - border);
                result.setRGB(x, y, isBlack ? 0x000000 : 0xFFFFFF);
            }
        }
        return result;
    }
}
