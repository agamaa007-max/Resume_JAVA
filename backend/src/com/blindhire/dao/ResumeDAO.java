package com.blindhire.dao;

import com.blindhire.database.DBConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ResumeDAO manages storage of uploaded resume files and their raw extracted text.
 */
public class ResumeDAO {

    public int saveResume(int candidateId, String fileName, String filePath, String fileType, String extractedText) throws SQLException {
        String sql = "INSERT INTO resumes (candidate_id, file_name, file_path, file_type, extracted_text) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, candidateId);
            ps.setString(2, fileName);
            ps.setString(3, filePath);
            ps.setString(4, fileType);
            ps.setString(5, extractedText);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public Map<String, Object> getResumeByCandidateId(int candidateId) throws SQLException {
        String sql = "SELECT id, candidate_id, file_name, file_path, file_type, uploaded_at FROM resumes WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("candidateId", rs.getInt("candidate_id"));
                    map.put("fileName", rs.getString("file_name"));
                    map.put("filePath", rs.getString("file_path"));
                    map.put("fileType", rs.getString("file_type"));
                    map.put("uploadedAt", rs.getTimestamp("uploaded_at"));
                    return map;
                }
            }
        }
        return null;
    }

    public Map<String, Object> getResumeById(int id) throws SQLException {
        String sql = "SELECT id, candidate_id, file_name, file_path, file_type, uploaded_at FROM resumes WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("candidateId", rs.getInt("candidate_id"));
                    map.put("fileName", rs.getString("file_name"));
                    map.put("filePath", rs.getString("file_path"));
                    map.put("fileType", rs.getString("file_type"));
                    map.put("uploadedAt", rs.getTimestamp("uploaded_at"));
                    return map;
                }
            }
        }
        return null;
    }
}
