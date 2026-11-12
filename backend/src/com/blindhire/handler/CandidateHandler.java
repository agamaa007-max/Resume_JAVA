package com.blindhire.handler;

import com.blindhire.model.Candidate;
import com.blindhire.model.CandidateIdentity;
import com.blindhire.security.AuthMiddleware;
import com.blindhire.security.SessionManager;
import com.blindhire.server.CORSHandler;
import com.blindhire.service.CandidateService;
import com.blindhire.service.IdentityService;
import com.blindhire.util.JSONUtil;
import com.blindhire.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CandidateHandler handles candidate inspection and authorized identity revelation.
 */
public class CandidateHandler implements HttpHandler {

    private final CandidateService candidateService = new CandidateService();
    private final IdentityService identityService = new IdentityService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (CORSHandler.handlePreflight(exchange)) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            if ("/api/candidates".equals(path) && "GET".equals(method)) {
                handleGetCandidates(exchange);
            } else if (path.matches("^/api/candidates/\\d+$") && "GET".equals(method)) {
                handleGetCandidateById(exchange, extractIdFromPath(path));
            } else if (path.matches("^/api/candidates/\\d+/reveal$") && "POST".equals(method)) {
                handleRevealIdentity(exchange, extractIdFromRevealPath(path));
            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        } catch (IllegalStateException e) {
            ResponseUtil.sendError(exchange, 404, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(exchange, 500, "Error handling candidate request: " + e.getMessage());
        }
    }

    private void handleGetCandidates(HttpExchange exchange) throws Exception {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI());

        Integer jobId = null;
        if (queryParams.containsKey("jobId") && !queryParams.get("jobId").isEmpty()) {
            try { jobId = Integer.parseInt(queryParams.get("jobId")); } catch (NumberFormatException ignored) {}
        }

        Double minScore = null;
        if (queryParams.containsKey("minScore") && !queryParams.get("minScore").isEmpty()) {
            try { minScore = Double.parseDouble(queryParams.get("minScore")); } catch (NumberFormatException ignored) {}
        }

        String skill = queryParams.get("skill");
        String search = queryParams.get("search");

        List<Map<String, Object>> list = candidateService.getScreenedCandidateSummaries(jobId, minScore, skill, search);
        ResponseUtil.sendSuccess(exchange, 200, "Candidates retrieved successfully.", list);
    }

    private void handleGetCandidateById(HttpExchange exchange, int candidateId) throws Exception {
        Candidate candidate = candidateService.getCandidateById(candidateId);
        if (candidate == null) {
            ResponseUtil.sendError(exchange, 404, "Candidate not found.");
            return;
        }
        // Notice: candidate personal identity is intentionally absent here
        ResponseUtil.sendSuccess(exchange, 200, "Anonymous candidate profile", candidate);
    }

    private void handleRevealIdentity(HttpExchange exchange, int candidateId) throws Exception {
        SessionManager.UserSession session = AuthMiddleware.authenticate(exchange);
        if (session == null) {
            ResponseUtil.sendError(exchange, 401, "Unauthorized. Recruiter authentication is required to reveal candidate identity.");
            return;
        }

        String body = readRequestBody(exchange);
        Map<String, Object> map = JSONUtil.toMap(body);
        if (map == null || !map.containsKey("reason")) {
            ResponseUtil.sendError(exchange, 400, "Justification reason is required to reveal candidate identity.");
            return;
        }

        String reason = (String) map.get("reason");
        CandidateIdentity identity = identityService.revealIdentity(
            candidateId,
            session.getUserId(),
            session.getEmail(),
            reason
        );

        ResponseUtil.sendSuccess(exchange, 200, "Identity successfully revealed and logged.", identity);
    }

    private Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> queryPairs = new HashMap<>();
        String query = uri.getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    try {
                        String key = java.net.URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                        String val = java.net.URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                        queryPairs.put(key, val);
                    } catch (Exception ignored) {}
                }
            }
        }
        return queryPairs;
    }

    private int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private int extractIdFromRevealPath(String path) {
        // e.g. /api/candidates/123/reveal
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 2]);
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
