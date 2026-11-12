package com.blindhire.security;

import com.sun.net.httpserver.HttpExchange;

/**
 * AuthMiddleware extracts and validates session tokens from HTTP requests.
 */
public class AuthMiddleware {

    public static final String SESSION_ATTR = "authenticated_user";

    /**
     * Extracts session from Authorization header or Cookie header.
     * @param exchange HttpExchange request
     * @return UserSession if valid, null otherwise
     */
    public static SessionManager.UserSession authenticate(HttpExchange exchange) {
        String token = extractToken(exchange);
        if (token == null) {
            return null;
        }
        SessionManager.UserSession session = SessionManager.getSession(token);
        if (session != null) {
            exchange.setAttribute(SESSION_ATTR, session);
        }
        return session;
    }

    private static String extractToken(HttpExchange exchange) {
        // 1. Check Authorization: Bearer <token>
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        // 2. Check X-Auth-Token header
        String customHeader = exchange.getRequestHeaders().getFirst("X-Auth-Token");
        if (customHeader != null && !customHeader.trim().isEmpty()) {
            return customHeader.trim();
        }

        // 3. Check Cookie header (e.g. auth_token=...)
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] pair = cookie.trim().split("=", 2);
                if (pair.length == 2 && "auth_token".equalsIgnoreCase(pair[0].trim())) {
                    return pair[1].trim();
                }
            }
        }

        return null;
    }
}
