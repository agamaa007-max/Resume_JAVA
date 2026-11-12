package com.blindhire.server;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

/**
 * CORSHandler manages Cross-Origin Resource Sharing headers
 * and handles HTTP OPTIONS preflight inquiries.
 */
public class CORSHandler {

    public static void applyHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Auth-Token");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");
    }

    public static boolean handlePreflight(HttpExchange exchange) throws IOException {
        applyHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }
}
