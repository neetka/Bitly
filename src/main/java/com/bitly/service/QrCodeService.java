package com.bitly.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating QR codes from URLs using the ZXing library.
 */
@Service
@Slf4j
public class QrCodeService {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Generates a QR code image for the given URL.
     *
     * @param url    the URL to encode in the QR code
     * @param width  the width of the QR code image in pixels
     * @param height the height of the QR code image in pixels
     * @return the QR code image as a PNG byte array
     * @throws WriterException if QR code generation fails
     * @throws IOException     if image writing fails
     */
    public byte[] generateQrCode(String url, int width, int height)
            throws WriterException, IOException {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);

        log.debug("Generated QR code for URL: {} ({}x{})", url, width, height);
        return outputStream.toByteArray();
    }

    /**
     * Generates a QR code image with default dimensions (300x300).
     *
     * @param url the URL to encode
     * @return the QR code image as a PNG byte array
     */
    public byte[] generateQrCode(String url) throws WriterException, IOException {
        return generateQrCode(url, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}
