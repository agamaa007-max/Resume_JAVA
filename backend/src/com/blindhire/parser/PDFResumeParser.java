package com.blindhire.parser;

import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * PDFResumeParser extracts text from PDF resumes using Apache PDFBox.
 */
public class PDFResumeParser implements ResumeParser {

    @Override
    public boolean supports(String extension) {
        return extension != null && extension.equalsIgnoreCase("pdf");
    }

    @Override
    public String extractText(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("PDF file does not exist.");
        }

        try (PDDocument document = PDDocument.load(file)) {
            if (document.isEncrypted()) {
                throw new IllegalStateException("Encrypted PDF files cannot be processed without a password.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            return text != null ? text.trim() : "";
        }
    }
}
