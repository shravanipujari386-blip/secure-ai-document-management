
package com.lawenforcement.service;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class DocumentTextExtractionService {

    public String extractText(byte[] fileBytes) throws IOException {

        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException(
                    "Please provide a non-empty PDF document.");
        }

        try (PDDocument document = Loader.loadPDF(fileBytes)) {

            if (document.isEncrypted()) {
                throw new IllegalArgumentException(
                        "Encrypted PDFs are not supported.");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            if (extractedText == null || extractedText.isBlank()) {
                throw new IllegalArgumentException(
                        "No readable text was found. "
                        + "The PDF may be scanned and require OCR.");
            }

            return extractedText.trim();
        }
    }
}

