package com.blindhire.handler;

import com.blindhire.config.AppConfig;
import com.blindhire.dao.ResumeDAO;
import com.blindhire.security.AuthMiddleware;
import com.blindhire.security.SessionManager;
import com.blindhire.server.CORSHandler;
import com.blindhire.service.ResumeService;
import com.blindhire.util.FileUtil;
import com.blindhire.util.JSONUtil;
import com.blindhire.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * ResumeHandler accepts and orchestrates resume uploads and text extraction.
 * Supports both JSON Base64 document payload and standard multipart stream uploads.
 */
public class ResumeHandler implements HttpHandler {

    private final ResumeService resumeService = new ResumeService();
    private final ResumeDAO resumeDAO = new ResumeDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (CORSHandler.handlePreflight(exchange)) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            if ("/api/resumes/upload".equals(path) && "POST".equals(method)) {
                handleResumeUpload(exchange);
            } else if (path.matches("^/api/resumes/\\d+$") && "GET".equals(method)) {
                handleGetResumeById(exchange, extractIdFromPath(path));
            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(exchange, 500, "Error during resume processing: " + e.getMessage());
        }
    }

    private void handleResumeUpload(HttpExchange exchange) throws Exception {
        SessionManager.UserSession session = AuthMiddleware.authenticate(exchange);
        if (session == null) {
            ResponseUtil.sendError(exchange, 401, "Authentication required to upload resumes.");
            return;
        }

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null) contentType = "";

        File targetFile = null;
        String originalFilename = "resume.pdf";
        int jobId = 1;

        File uploadDir = new File(AppConfig.getUploadDir());
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        if (contentType.toLowerCase().contains("application/json")) {
            // High-reliability JSON Base64 upload
            String body = readRequestBody(exchange);
            Map<String, Object> map = JSONUtil.toMap(body);
            if (map == null) {
                ResponseUtil.sendError(exchange, 400, "Invalid JSON upload payload.");
                return;
            }

            if (!map.containsKey("fileBase64") || !map.containsKey("jobId")) {
                ResponseUtil.sendError(exchange, 400, "Payload must include 'fileBase64', 'jobId', and 'fileName'.");
                return;
            }

            originalFilename = (String) map.getOrDefault("fileName", "resume.pdf");
            Object jIdObj = map.get("jobId");
            jobId = (jIdObj instanceof Number) ? ((Number) jIdObj).intValue() : Integer.parseInt(jIdObj.toString());

            String base64Content = (String) map.get("fileBase64");
            // If Data URL prefix exists (e.g. data:application/pdf;base64,...), strip it
            if (base64Content.contains(",")) {
                base64Content = base64Content.substring(base64Content.indexOf(",") + 1);
            }

            byte[] fileBytes = Base64.getDecoder().decode(base64Content);
            String safeStoredName = FileUtil.generateUniqueFilename(originalFilename);
            targetFile = new File(uploadDir, safeStoredName);

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(fileBytes);
            }
        } else if (contentType.toLowerCase().contains("multipart/form-data")) {
            // Parse multipart body
            MultipartData parsed = parseMultipart(exchange.getRequestBody(), contentType, uploadDir);
            targetFile = parsed.file;
            originalFilename = parsed.originalFilename;
            jobId = parsed.jobId;
        } else {
            ResponseUtil.sendError(exchange, 415, "Unsupported media type. Send application/json or multipart/form-data.");
            return;
        }

        // Process the uploaded file through the resume pipeline
        Map<String, Object> result = resumeService.processUploadedResume(
            targetFile,
            originalFilename,
            jobId,
            session.getUserId()
        );

        ResponseUtil.sendSuccess(exchange, 201, "Resume uploaded, extracted and screened successfully.", result);
    }

    private void handleGetResumeById(HttpExchange exchange, int id) throws Exception {
        SessionManager.UserSession session = AuthMiddleware.authenticate(exchange);
        if (session == null) {
            ResponseUtil.sendError(exchange, 401, "Authentication required.");
            return;
        }

        Map<String, Object> resume = resumeDAO.getResumeById(id);
        if (resume == null) {
            ResponseUtil.sendError(exchange, 404, "Resume record not found.");
            return;
        }

        ResponseUtil.sendSuccess(exchange, 200, "Resume metadata", resume);
    }

    private int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static class MultipartData {
        File file;
        String originalFilename = "resume.pdf";
        int jobId = 1;
    }

    private MultipartData parseMultipart(InputStream input, String contentType, File uploadDir) throws IOException {
        String boundary = "";
        for (String param : contentType.split(";")) {
            param = param.trim();
            if (param.startsWith("boundary=")) {
                boundary = "--" + param.substring("boundary=".length());
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        byte[] rawBytes = baos.toByteArray();

        MultipartData data = new MultipartData();
        String rawStr = new String(rawBytes, StandardCharsets.ISO_8859_1);

        String[] parts = rawStr.split(boundary);
        for (String part : parts) {
            if (part.contains("name=\"jobId\"")) {
                int bodyStart = part.indexOf("\r\n\r\n");
                if (bodyStart >= 0) {
                    String val = part.substring(bodyStart + 4).trim();
                    try { data.jobId = Integer.parseInt(val.split("\r\n")[0]); } catch (Exception ignored) {}
                }
            } else if (part.contains("filename=\"")) {
                int fnStart = part.indexOf("filename=\"") + 10;
                int fnEnd = part.indexOf("\"", fnStart);
                if (fnStart >= 10 && fnEnd > fnStart) {
                    data.originalFilename = part.substring(fnStart, fnEnd);
                }

                int bodyStart = part.indexOf("\r\n\r\n");
                if (bodyStart >= 0) {
                    int bodyEnd = part.lastIndexOf("\r\n");
                    if (bodyEnd > bodyStart) {
                        String filePartStr = part.substring(bodyStart + 4, bodyEnd);
                        byte[] fileBytes = filePartStr.getBytes(StandardCharsets.ISO_8859_1);
                        String safeName = FileUtil.generateUniqueFilename(data.originalFilename);
                        data.file = new File(uploadDir, safeName);
                        try (FileOutputStream fos = new FileOutputStream(data.file)) {
                            fos.write(fileBytes);
                        }
                    }
                }
            }
        }

        if (data.file == null) {
            throw new IllegalArgumentException("No file found in multipart upload.");
        }

        return data;
    }
}
