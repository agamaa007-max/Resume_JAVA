package com.blindhire.handler;

import com.blindhire.model.User;
import com.blindhire.security.AuthMiddleware;
import com.blindhire.security.SessionManager;
import com.blindhire.server.CORSHandler;
import com.blindhire.service.AuthService;
import com.blindhire.util.JSONUtil;
import com.blindhire.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * AuthHandler handles registration, login, logout, and recruiter profile inspection.
 */
public class AuthHandler implements HttpHandler {

    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (CORSHandler.handlePreflight(exchange)) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            if ("/api/auth/register".equals(path) && "POST".equals(method)) {
                handleRegister(exchange);
            } else if ("/api/auth/login".equals(path) && "POST".equals(method)) {
                handleLogin(exchange);
            } else if ("/api/auth/logout".equals(path) && "POST".equals(method)) {
                handleLogout(exchange);
            } else if ("/api/auth/me".equals(path) && "GET".equals(method)) {
                handleMe(exchange);
            } else if ("/api/auth/password".equals(path) && "POST".equals(method)) {
                handleChangePassword(exchange);
            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        } catch (IllegalStateException e) {
            ResponseUtil.sendError(exchange, 409, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(exchange, 500, "Authentication internal error: " + e.getMessage());
        }
    }

    private void handleRegister(HttpExchange exchange) throws Exception {
        String body = readRequestBody(exchange);
        Map<String, Object> map = JSONUtil.toMap(body);
        if (map == null) {
            ResponseUtil.sendError(exchange, 400, "Invalid JSON body.");
            return;
        }

        String fullName = (String) map.get("fullName");
        String email = (String) map.get("email");
        String password = (String) map.get("password");

        Map<String, Object> authData = authService.register(fullName, email, password);
        ResponseUtil.sendSuccess(exchange, 201, "Registration successful.", authData);
    }

    private void handleLogin(HttpExchange exchange) throws Exception {
        String body = readRequestBody(exchange);
        Map<String, Object> map = JSONUtil.toMap(body);
        if (map == null) {
            ResponseUtil.sendError(exchange, 400, "Invalid JSON body.");
            return;
        }

        String email = (String) map.get("email");
        String password = (String) map.get("password");

        Map<String, Object> authData = authService.login(email, password);
        ResponseUtil.sendSuccess(exchange, 200, "Login successful.", authData);
    }

    private void handleLogout(HttpExchange exchange) throws Exception {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            SessionManager.invalidateSession(authHeader.substring(7).trim());
        }
        ResponseUtil.sendSuccess(exchange, 200, "Logged out successfully.", null);
    }

    private void handleMe(HttpExchange exchange) throws Exception {
        SessionManager.UserSession session = AuthMiddleware.authenticate(exchange);
        if (session == null) {
            ResponseUtil.sendError(exchange, 401, "Unauthorized. Please log in.");
            return;
        }

        User user = authService.getProfile(session.getUserId());
        if (user == null) {
            ResponseUtil.sendError(exchange, 404, "User profile not found.");
            return;
        }
        ResponseUtil.sendSuccess(exchange, 200, "Current authenticated recruiter", user);
    }

    private void handleChangePassword(HttpExchange exchange) throws Exception {
        SessionManager.UserSession session = AuthMiddleware.authenticate(exchange);
        if (session == null) {
            ResponseUtil.sendError(exchange, 401, "Unauthorized. Please log in.");
            return;
        }

        String body = readRequestBody(exchange);
        Map<String, Object> map = JSONUtil.toMap(body);
        String currentPassword = (String) map.get("currentPassword");
        String newPassword = (String) map.get("newPassword");

        boolean changed = authService.changePassword(session.getUserId(), currentPassword, newPassword);
        if (changed) {
            ResponseUtil.sendSuccess(exchange, 200, "Password updated successfully.", null);
        } else {
            ResponseUtil.sendError(exchange, 400, "Failed to update password.");
        }
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
}
