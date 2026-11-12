package com.blindhire.handler;

import com.blindhire.dao.CandidateDAO;
import com.blindhire.dao.JobDAO;
import com.blindhire.dao.ScreeningDAO;
import com.blindhire.model.Candidate;
import com.blindhire.model.Job;
import com.blindhire.model.ScreeningResult;
import com.blindhire.server.CORSHandler;
import com.blindhire.service.MatchingService;
import com.blindhire.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ScreeningHandler serves dashboard metrics and candidate screening results.
 */
public class ScreeningHandler implements HttpHandler {

    private final ScreeningDAO screeningDAO = new ScreeningDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final JobDAO jobDAO = new JobDAO();
    private final MatchingService matchingService = new MatchingService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (CORSHandler.handlePreflight(exchange)) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            if ("/api/screening/dashboard".equals(path) && "GET".equals(method)) {
                handleDashboardStats(exchange);
            } else if (path.matches("^/api/screening/job/\\d+$") && "GET".equals(method)) {
                handleScreeningByJob(exchange, extractId(path));
            } else if (path.matches("^/api/screening/candidate/\\d+$") && "GET".equals(method)) {
                handleScreeningByCandidate(exchange, extractId(path));
            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(exchange, 500, "Error in screening handler: " + e.getMessage());
        }
    }

    private void handleDashboardStats(HttpExchange exchange) throws Exception {
        Map<String, Object> stats = screeningDAO.getDashboardStats();
        ResponseUtil.sendSuccess(exchange, 200, "Dashboard statistics retrieved from database", stats);
    }

    private void handleScreeningByJob(HttpExchange exchange, int jobId) throws Exception {
        List<ScreeningResult> results = screeningDAO.getScreeningResultsByJob(jobId);
        ResponseUtil.sendSuccess(exchange, 200, "Screening results for job " + jobId, results);
    }

    private void handleScreeningByCandidate(HttpExchange exchange, int candidateId) throws Exception {
        Map<String, String> qp = parseQueryParams(exchange.getRequestURI());
        int jobId = 1;
        if (qp.containsKey("jobId")) {
            try { jobId = Integer.parseInt(qp.get("jobId")); } catch (Exception ignored) {}
        }

        ScreeningResult sr = screeningDAO.getScreeningResultByCandidate(candidateId, jobId);
        if (sr == null) {
            // Calculate dynamically if not already saved in database
            Candidate candidate = candidateDAO.getCandidateById(candidateId);
            Job job = jobDAO.getJobById(jobId);
            if (candidate != null && job != null) {
                sr = matchingService.calculateScreening(candidate, job);
                screeningDAO.saveScreeningResult(sr);
            }
        }

        if (sr == null) {
            ResponseUtil.sendError(exchange, 404, "Screening result could not be found or computed.");
            return;
        }

        ResponseUtil.sendSuccess(exchange, 200, "Candidate screening score details", sr);
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
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
}
