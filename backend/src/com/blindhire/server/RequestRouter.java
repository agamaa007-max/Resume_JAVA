package com.blindhire.server;

import com.blindhire.handler.*;
import com.blindhire.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * RequestRouter directs incoming HTTP requests:
 * - /api/* requests are dispatched to REST handlers
 * - All other paths serve static frontend web assets (HTML, CSS, JS)
 */
public class RequestRouter implements HttpHandler {

    private final AuthHandler authHandler = new AuthHandler();
    private final JobHandler jobHandler = new JobHandler();
    private final ResumeHandler resumeHandler = new ResumeHandler();
    private final CandidateHandler candidateHandler = new CandidateHandler();
    private final ScreeningHandler screeningHandler = new ScreeningHandler();
    private final AuditHandler auditHandler = new AuditHandler();

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("html", "text/html; charset=UTF-8");
        MIME_TYPES.put("htm", "text/html; charset=UTF-8");
        MIME_TYPES.put("css", "text/css; charset=UTF-8");
        MIME_TYPES.put("js", "application/javascript; charset=UTF-8");
        MIME_TYPES.put("json", "application/json; charset=UTF-8");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("pdf", "application/pdf");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // 1. API Routing
        if (path.startsWith("/api/auth")) {
            authHandler.handle(exchange);
        } else if (path.startsWith("/api/jobs")) {
            jobHandler.handle(exchange);
        } else if (path.startsWith("/api/resumes")) {
            resumeHandler.handle(exchange);
        } else if (path.startsWith("/api/candidates")) {
            candidateHandler.handle(exchange);
        } else if (path.startsWith("/api/screening")) {
            screeningHandler.handle(exchange);
        } else if (path.startsWith("/api/audit-logs")) {
            auditHandler.handle(exchange);
        } else {
            // 2. Static File Serving (Frontend)
            serveStaticFile(exchange, path);
        }
    }

    private void serveStaticFile(HttpExchange exchange, String requestPath) throws IOException {
        if (CORSHandler.handlePreflight(exchange)) {
            return;
        }

        // Map root / to index.html
        if (requestPath == null || requestPath.equals("/") || requestPath.isEmpty()) {
            requestPath = "/index.html";
        }

        // Prevent directory traversal
        if (requestPath.contains("..")) {
            ResponseUtil.sendError(exchange, 403, "Forbidden path access.");
            return;
        }

        // Resolve file from frontend directory
        File staticDir = new File("frontend");
        if (!staticDir.exists()) {
            staticDir = new File(".." + File.separator + "frontend");
        }

        File targetFile = new File(staticDir, requestPath.startsWith("/") ? requestPath.substring(1) : requestPath);

        if (!targetFile.exists() || targetFile.isDirectory()) {
            // Check if adding .html helps (e.g. /dashboard -> /dashboard.html)
            File htmlAlternative = new File(targetFile.getAbsolutePath() + ".html");
            if (htmlAlternative.exists()) {
                targetFile = htmlAlternative;
            } else {
                ResponseUtil.sendError(exchange, 404, "Page or static resource not found: " + requestPath);
                return;
            }
        }

        // Determine MIME Content-Type
        String ext = "";
        int dot = targetFile.getName().lastIndexOf('.');
        if (dot >= 0) {
            ext = targetFile.getName().substring(dot + 1).toLowerCase();
        }
        String contentType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");

        exchange.getResponseHeaders().set("Content-Type", contentType);
        CORSHandler.applyHeaders(exchange);

        byte[] fileBytes = new byte[(int) targetFile.length()];
        try (FileInputStream fis = new FileInputStream(targetFile)) {
            fis.read(fileBytes);
        }

        exchange.sendResponseHeaders(200, fileBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(fileBytes);
        }
    }
}
