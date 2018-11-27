package com.capitalone.creditocr.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.lang.NonNull;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PdfUtil {

    /**
     * Magic number in PDF header file. Maps to ASCII characters "%PDF"
     */
    private static final byte[] PDF_HEADER = {0x25, 0x50, 0x44, 0x46};
    private static final String FILE_FORMAT = "PNG";


    public static boolean isPdf(byte[] buffer) {
        if (buffer == null || buffer.length < 4) {
            return false;
        }

        byte[] header = new byte[PDF_HEADER.length];
        System.arraycopy(buffer, 0, header, 0, header.length);

        return Arrays.equals(PDF_HEADER, header);
    }

    /**
     * Convert a PDF document into multiple images.
     * @param content The content of the PDF
     * @return A PNG image for each page
     */
    public static List<byte[]> pdf2png(@NonNull byte[] content) throws IOException {

        if (!isPdf(content)) {
            throw new IllegalArgumentException("Content is not in PDF format");
        }

        PDDocument doc = PDDocument.load(content);
        PDFRenderer renderer = new PDFRenderer(doc);
        List<byte[]> ret = new ArrayList<>();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            var tmp = renderer.renderImage(i, 2.5f);
            bytes.reset();
            ImageIO.write(tmp, FILE_FORMAT, bytes);
            ret.add(bytes.toByteArray());
        }
        doc.close();
        bytes.close();


        return ret;
    }
}
