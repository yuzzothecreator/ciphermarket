package com.ciphermarket.api.delivery.transform;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfWatermarkService {

    public byte[] watermark(InputStream pdfInput, String watermarkText) {
        try (PDDocument document = Loader.loadPDF(pdfInput.readAllBytes());
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            for (PDPage page : document.getPages()) {
                PDRectangle box = page.getMediaBox();
                try (PDPageContentStream content = new PDPageContentStream(
                        document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    content.setFont(font, 10);
                    content.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                    content.beginText();
                    content.newLineAtOffset(36, box.getHeight() - 36);
                    content.showText(watermarkText);
                    content.endText();
                }
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to watermark PDF", e);
        }
    }

    public InputStream watermarkStream(byte[] pdfBytes, String watermarkText) {
        return new ByteArrayInputStream(watermark(new ByteArrayInputStream(pdfBytes), watermarkText));
    }
}
