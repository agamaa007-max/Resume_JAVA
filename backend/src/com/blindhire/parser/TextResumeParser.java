package com.blindhire.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * TextResumeParser extracts text from plain text or fallback document files.
 */
public class TextResumeParser implements ResumeParser {

    @Override
    public boolean supports(String extension) {
        return extension != null && (extension.equalsIgnoreCase("txt") || extension.equalsIgnoreCase("text"));
    }

    @Override
    public String extractText(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Text file does not exist.");
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }
}
