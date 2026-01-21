package com.project.cinemabackend.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class QrGenerator {
    private static final int WIDTH = 200;
    private static final int HEIGHT = 200;

    static public byte[] generateQr(String code) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(code, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);

            return output.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error while generating qr code.", e);
        }
    }
}
