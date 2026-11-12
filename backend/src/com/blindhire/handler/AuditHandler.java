package com.blindhire.handler;

import com.blindhire.security.AuthMiddleware;
import com.blindhire.security.SessionManager;
import com.blindhire.server.CORSHandler;
import com.blindhire.service.AuditService;
import com.blindhire.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * AuditHandler serves audit log trail records to authenticated recruiters.
 */
public class AuditHandler implements HttpHandler {

    private final AuditService auditService = new AuditService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (CORSHandler.handlePreflight(exchange)) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            if ("/api/audit-logs".equals(path) && "GET".equals(method)) {
                SessionManager.UserSession session = AuthMiddleware.authenticate(exchange);
                if (session == null) {
                    ResponseUtil.sendError(exchange, 401, "Authentication required to access system audit logs.");
                    return;
                }

                List<Map<String, Object>> logs = auditService.getAuditLogs(100);
                ResponseUtil.sendSuccess(exchange, 200, "Audit logs retrieved.", logs);
            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(exchange, 500, "Error fetching audit logs: " + e.getMessage());
        }
    }
}
