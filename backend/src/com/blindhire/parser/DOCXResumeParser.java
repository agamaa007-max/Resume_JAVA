package com.blindhire.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * DOCXResumeParser extracts textual content from Microsoft Word .docx documents.
 * Employs Apache POI XWPF, backed by a pure Core Java Zip/XML stream fallback.
 */
public class DOCXResumeParser implements ResumeParser {

    @Override
    public boolean supports(String extension) {
        return extension != null && extension.equalsIgnoreCase("docx");
    }

    @Override
    public String extractText(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("DOCX file does not exist.");
        }

        try {
            // Primary parser: Apache POI
            try (FileInputStream fis = new FileInputStream(file);
                 XWPFDocument document = new XWPFDocument(fis);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                return extractor.getText();
            }
        } catch (Throwable t) {
            System.err.println("[DOCX-PARSER] POI extraction notice: " + t.getMessage() + ". Using resilient XML stream fallback.");
            return extractTextFromZipXml(file);
        }
    }

    /**
     * Resilient Core Java fallback: A .docx file is a zip archive containing word/document.xml.
     * Extracts text by stripping XML tags from the document stream.
     */
    private String extractTextFromZipXml(File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equalsIgnoreCase(entry.getName())) {
                    byte[] buffer = new byte[4096];
                    int read;
                    StringBuilder xmlSb = new StringBuilder();
                    while ((read = zis.read(buffer)) != -1) {
                        xmlSb.append(new String(buffer, 0, read, "UTF-8"));
                    }
                    // Strip XML tags: <w:p> becomes newline, other tags are removed
                    String xml = xmlSb.toString();
                    xml = xml.replaceAll("</w:p>", "\n");
                    xml = xml.replaceAll("<[^>]+>", " ");
                    // Decode common XML entities
                    xml = xml.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'");
                    sb.append(xml);
                    break;
                }
            }
        }
        return sb.toString().trim();
    }
}
