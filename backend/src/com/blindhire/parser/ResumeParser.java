package com.blindhire.parser;

import java.io.File;

/**
 * Common interface for resume text extraction engines.
 * Demonstrates:
 * - Abstraction and Polymorphism
 * - Extensible parser strategy
 */
public interface ResumeParser {
    /**
     * Extracts all raw textual content from the supplied resume file.
     * @param file Target document file
     * @return Extracted plain text string
     * @throws Exception if extraction fails
     */
    String extractText(File file) throws Exception;

    /**
     * Checks if this parser supports the given file extension.
     */
    boolean supports(String extension);
}
