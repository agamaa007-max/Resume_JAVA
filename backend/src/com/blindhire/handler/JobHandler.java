package com.blindhire.handler;

import com.blindhire.model.Job;
import com.blindhire.model.JobSkill;
import com.blindhire.security.AuthMiddleware;
import com.blindhire.security.SessionManager;
import com.blindhire.server.CORSHandler;
import com.blindhire.service.JobService;
import com.blindhire.util.JSONUtil;
import com.blindhire.util.ResponseUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * JobHandler processes REST operations for job postings.
 */
public class JobHandler implements HttpHandler {

    private final JobService jobService = new JobService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (CORSHandler.handlePreflight(exchange)) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            if ("/api/jobs".equals(path)) {
                if ("GET".equals(method)) {
                    handleGetAllJobs(exchange);
                } else if ("POST".equals(method)) {
                    handleCreateJob(exchange);
                } else {
                    ResponseUtil.sendError(exchange, 405, "Method Not Allowed");
                }
            } else if (path.matches("^/api/jobs/\\d+$")) {
                int jobId = extractIdFromPath(path);
                if ("GET".equals(method)) {
                    handleGetJobById(exchange, jobId);
                } else if ("PUT".equals(method)) {
                    handleUpdateJob(exchange, jobId);
                } else if ("DELETE".equals(method)) {
                    handleDeleteJob(exchange, jobId);
                } else {
                    ResponseUtil.sendError(exchange, 405, "Method Not Allowed");
                }
            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint Not Found");
            }
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(exchange, 500, "Error processing job request: " + e.getMessage());
        }
    }

    private void handleGetAllJobs(HttpExchange exchange) throws Exception {
        List<Job> jobs = jobService.getAllJobs();
        ResponseUtil.sendSuccess(exchange, 200, "Jobs retrieved successfully", jobs);
    }

    private void handleGetJobById(HttpExchange exchange, int jobId) throws Exception {
        Job job = jobService.getJobById(jobId);
        if (job == null) {
            ResponseUtil.sendError(exchange, 404, "Job with ID " + jobId + " not found.");
            return;
        }
        ResponseUtil.sendSuccess(exchange, 200, "Job details retrieved", job);
    }

    private void handleCreateJob(HttpExchange exchange) throws Exception {
        SessionManager.UserSession session = AuthMiddleware.authenticate(exchange);
        if (session == null) {
            ResponseUtil.sendError(exchange, 401, "Authentication required to create a job.");
            return;
        }

        String body = readRequestBody(exchange);
        Job job = parseJobFromJson(body);

        Job created = jobService.createJob(job, session.getUserId());
        ResponseUtil.sendSuccess(exchange, 201, "Job posting created successfully.", created);
    }

    private void handleUpdateJob(HttpExchange exchange, int jobId) throws Exception {
        SessionManager.UserSession session = AuthMiddleware.authenticate(exchange);
        if (session == null) {
            ResponseUtil.sendError(exchange, 401, "Authentication required to update a job.");
            return;
        }

        String body = readRequestBody(exchange);
        Job job = parseJobFromJson(body);
        job.setId(jobId);

        boolean updated = jobService.updateJob(job, session.getUserId());
        if (updated) {
            ResponseUtil.sendSuccess(exchange, 200, "Job updated successfully.", job);
        } else {
            ResponseUtil.sendError(exchange, 400, "Failed to update job.");
        }
    }

    private void handleDeleteJob(HttpExchange exchange, int jobId) throws Exception {
        SessionManager.UserSession session = AuthMiddleware.authenticate(exchange);
        if (session == null) {
            ResponseUtil.sendError(exchange, 401, "Authentication required to delete a job.");
            return;
        }

        boolean deleted = jobService.deleteJob(jobId, session.getUserId());
        if (deleted) {
            ResponseUtil.sendSuccess(exchange, 200, "Job deleted successfully.", null);
        } else {
            ResponseUtil.sendError(exchange, 404, "Job not found or already deleted.");
        }
    }

    private Job parseJobFromJson(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        Job job = new Job();
        if (obj.has("title")) job.setTitle(obj.get("title").getAsString());
        if (obj.has("description")) job.setDescription(obj.get("description").getAsString());
        if (obj.has("minimumEducation")) job.setMinimumEducation(obj.get("minimumEducation").getAsString());
        if (obj.has("minimumExperience")) job.setMinimumExperience(obj.get("minimumExperience").getAsInt());
        if (obj.has("status")) job.setStatus(obj.get("status").getAsString());

        List<JobSkill> skills = new ArrayList<>();

        // Support requiredSkills array of strings
        if (obj.has("requiredSkills") && obj.get("requiredSkills").isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray("requiredSkills");
            for (JsonElement el : arr) {
                String s = el.getAsString().trim();
                if (!s.isEmpty()) skills.add(new JobSkill(s, "REQUIRED"));
            }
        }

        // Support preferredSkills array of strings
        if (obj.has("preferredSkills") && obj.get("preferredSkills").isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray("preferredSkills");
            for (JsonElement el : arr) {
                String s = el.getAsString().trim();
                if (!s.isEmpty()) skills.add(new JobSkill(s, "PREFERRED"));
            }
        }

        // Also support single comma-separated skill string if passed
        if (skills.isEmpty() && obj.has("skills") && obj.get("skills").isJsonPrimitive()) {
            String[] tokens = obj.get("skills").getAsString().split(",");
            for (String t : tokens) {
                String s = t.trim();
                if (!s.isEmpty()) skills.add(new JobSkill(s, "REQUIRED"));
            }
        }

        job.setSkills(skills);
        return job;
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
}
