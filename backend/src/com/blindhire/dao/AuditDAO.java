package com.blindhire.dao;

import com.blindhire.database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AuditDAO persists all audit log entries, specifically tracking identity reveals and security events.
 */
public class AuditDAO {

    public void logAction(Integer userId, String action, Integer candidateId, String description) {
        String sql = "INSERT INTO audit_logs (user_id, action, candidate_id, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setString(2, action);

            if (candidateId != null) {
                ps.setInt(3, candidateId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AUDIT] Error writing audit log: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getAllLogs(int limit) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT a.id, a.user_id, u.full_name AS user_name, u.email AS user_email, " +
                     "a.action, a.candidate_id, c.candidate_code, a.description, a.timestamp " +
                     "FROM audit_logs a " +
                     "LEFT JOIN users u ON a.user_id = u.id " +
                     "LEFT JOIN candidates c ON a.candidate_id = c.id " +
                     "ORDER BY a.timestamp DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 100);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("userId", rs.getObject("user_id"));
                    map.put("userName", rs.getString("user_name") != null ? rs.getString("user_name") : "System");
                    map.put("userEmail", rs.getString("user_email") != null ? rs.getString("user_email") : "-");
                    map.put("action", rs.getString("action"));
                    map.put("candidateId", rs.getObject("candidate_id"));
                    map.put("candidateCode", rs.getString("candidate_code") != null ? rs.getString("candidate_code") : "-");
                    map.put("description", rs.getString("description"));
                    map.put("timestamp", rs.getTimestamp("timestamp"));
                    list.add(map);
                }
            }
        }
        return list;
    }
}
