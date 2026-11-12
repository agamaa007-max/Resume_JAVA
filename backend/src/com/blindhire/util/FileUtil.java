package com.blindhire.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * FileUtil provides safe file operations, path sanitization, and magic-byte validation.
 */
public class FileUtil {

    private static final byte[] PDF_MAGIC = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] ZIP_MAGIC = new byte[]{0x50, 0x4B, 0x03, 0x04}; // PK.. (DOCX is a zip archive)

    /**
     * Sanitizes a filename to prevent directory traversal attacks.
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) return "unknown_file";
        File f = new File(filename);
        String name = f.getName();
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Generates a safe, unique filename preserving original extension.
     */
    public static String generateUniqueFilename(String originalFilename) {
        String clean = sanitizeFilename(originalFilename);
        String extension = "";
        int dotIdx = clean.lastIndexOf('.');
        if (dotIdx >= 0) {
            extension = clean.substring(dotIdx).toLowerCase();
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * Validates whether file is a real PDF or DOCX file using magic bytes, not just extension.
     */
    public static boolean isValidDocument(File file, String declaredType) {
        if (file == null || !file.exists() || file.length() == 0) {
            return false;
        }

        byte[] header = new byte[4];
        try (FileInputStream fis = new FileInputStream(file)) {
            int read = fis.read(header);
            if (read < 4) return false;

            if ("PDF".equalsIgnoreCase(declaredType)) {
                return header[0] == PDF_MAGIC[0] && header[1] == PDF_MAGIC[1] &&
                       header[2] == PDF_MAGIC[2] && header[3] == PDF_MAGIC[3];
            } else if ("DOCX".equalsIgnoreCase(declaredType)) {
                return header[0] == ZIP_MAGIC[0] && header[1] == ZIP_MAGIC[1] &&
                       header[2] == ZIP_MAGIC[2] && header[3] == ZIP_MAGIC[3];
            }
        } catch (IOException e) {
            return false;
        }

        return false;
    }
}
