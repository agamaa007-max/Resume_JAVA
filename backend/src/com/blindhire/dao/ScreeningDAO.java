package com.blindhire.dao;

import com.blindhire.database.DBConnection;
import com.blindhire.model.ScreeningResult;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ScreeningDAO manages persistence and aggregation for candidate screening evaluations and dashboard metrics.
 */
public class ScreeningDAO {

    public ScreeningResult saveScreeningResult(ScreeningResult result) throws SQLException {
        // Upsert based on candidate_id and job_id
        String checkSql = "SELECT id FROM screening_results WHERE candidate_id = ? AND job_id = ?";
        Integer existingId = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, result.getCandidateId());
            ps.setInt(2, result.getJobId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    existingId = rs.getInt("id");
                }
            }
        }

        if (existingId != null) {
            String updateSql = "UPDATE screening_results SET skill_score = ?, experience_score = ?, " +
                               "education_score = ?, project_score = ?, final_score = ?, screened_at = CURRENT_TIMESTAMP " +
                               "WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setDouble(1, result.getSkillScore());
                ps.setDouble(2, result.getExperienceScore());
                ps.setDouble(3, result.getEducationScore());
                ps.setDouble(4, result.getProjectScore());
                ps.setDouble(5, result.getFinalScore());
                ps.setInt(6, existingId);
                ps.executeUpdate();
                result.setId(existingId);
            }
        } else {
            String insertSql = "INSERT INTO screening_results (candidate_id, job_id, skill_score, experience_score, " +
                               "education_score, project_score, final_score) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, result.getCandidateId());
                ps.setInt(2, result.getJobId());
                ps.setDouble(3, result.getSkillScore());
                ps.setDouble(4, result.getExperienceScore());
                ps.setDouble(5, result.getEducationScore());
                ps.setDouble(6, result.getProjectScore());
                ps.setDouble(7, result.getFinalScore());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        result.setId(rs.getInt(1));
                    }
                }
            }
        }
        return result;
    }

    public List<ScreeningResult> getScreeningResultsByJob(int jobId) throws SQLException {
        List<ScreeningResult> list = new ArrayList<>();
        String sql = "SELECT sr.id, sr.candidate_id, sr.job_id, sr.skill_score, sr.experience_score, " +
                     "sr.education_score, sr.project_score, sr.final_score, sr.screened_at, " +
                     "c.candidate_code, j.title AS job_title " +
                     "FROM screening_results sr " +
                     "JOIN candidates c ON sr.candidate_id = c.id " +
                     "JOIN jobs j ON sr.job_id = j.id " +
                     "WHERE sr.job_id = ? " +
                     "ORDER BY sr.final_score DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ScreeningResult res = mapRow(rs);
                    list.add(res);
                }
            }
        }
        return list;
    }

    public ScreeningResult getScreeningResultByCandidate(int candidateId, int jobId) throws SQLException {
        String sql = "SELECT sr.id, sr.candidate_id, sr.job_id, sr.skill_score, sr.experience_score, " +
                     "sr.education_score, sr.project_score, sr.final_score, sr.screened_at, " +
                     "c.candidate_code, j.title AS job_title " +
                     "FROM screening_results sr " +
                     "JOIN candidates c ON sr.candidate_id = c.id " +
                     "JOIN jobs j ON sr.job_id = j.id " +
                     "WHERE sr.candidate_id = ? AND sr.job_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<ScreeningResult> getAllScreeningResults() throws SQLException {
        List<ScreeningResult> list = new ArrayList<>();
        String sql = "SELECT sr.id, sr.candidate_id, sr.job_id, sr.skill_score, sr.experience_score, " +
                     "sr.education_score, sr.project_score, sr.final_score, sr.screened_at, " +
                     "c.candidate_code, j.title AS job_title " +
                     "FROM screening_results sr " +
                     "JOIN candidates c ON sr.candidate_id = c.id " +
                     "JOIN jobs j ON sr.job_id = j.id " +
                     "ORDER BY sr.screened_at DESC LIMIT 50";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {
            // Total Jobs
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM jobs")) {
                if (rs.next()) stats.put("totalJobs", rs.getInt(1));
            }

            // Total Candidates
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM candidates")) {
                if (rs.next()) stats.put("totalCandidates", rs.getInt(1));
            }

            // Total Resumes Uploaded
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM resumes")) {
                if (rs.next()) stats.put("totalResumes", rs.getInt(1));
            }

            // Total Screened Candidates
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(DISTINCT candidate_id) FROM screening_results")) {
                if (rs.next()) stats.put("totalScreened", rs.getInt(1));
            }

            // Average Match Score
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COALESCE(AVG(final_score), 0.0) FROM screening_results")) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    stats.put("averageScore", Math.round(avg * 10.0) / 10.0);
                }
            }

            // Active Jobs count
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM jobs WHERE status = 'ACTIVE'")) {
                if (rs.next()) stats.put("activeJobs", rs.getInt(1));
            }
        }

        return stats;
    }

    private ScreeningResult mapRow(ResultSet rs) throws SQLException {
        ScreeningResult res = new ScreeningResult();
        res.setId(rs.getInt("id"));
        res.setCandidateId(rs.getInt("candidate_id"));
        res.setJobId(rs.getInt("job_id"));
        res.setSkillScore(rs.getDouble("skill_score"));
        res.setExperienceScore(rs.getDouble("experience_score"));
        res.setEducationScore(rs.getDouble("education_score"));
        res.setProjectScore(rs.getDouble("project_score"));
        res.setFinalScore(rs.getDouble("final_score"));
        res.setScreenedAt(rs.getTimestamp("screened_at"));
        res.setCandidateCode(rs.getString("candidate_code"));
        res.setJobTitle(rs.getString("job_title"));
        return res;
    }
}
