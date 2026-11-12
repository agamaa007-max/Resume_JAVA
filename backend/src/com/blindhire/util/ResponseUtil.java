package com.blindhire.util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ResponseUtil standardizes API responses across all handlers.
 * 
 * Response shape:
 * {
 *   "success": true | false,
 *   "message": "Human readable status",
 *   "data": { ... }
 * }
 */
public class ResponseUtil {

    public static void sendSuccess(HttpExchange exchange, int statusCode, String message, Object data) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", message);
        body.put("data", data);

        sendJsonResponse(exchange, statusCode, body);
    }

    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("data", null);

        sendJsonResponse(exchange, statusCode, body);
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object bodyObject) throws IOException {
        String json = JSONUtil.toJson(bodyObject);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Auth-Token");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
