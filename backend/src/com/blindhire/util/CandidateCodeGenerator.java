package com.blindhire.util;

import com.blindhire.database.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * CandidateCodeGenerator generates unique, non-identifying IDs (e.g. CAND-1001).
 */
public class CandidateCodeGenerator {

    private static final String PREFIX = "CAND-";
    private static final int INITIAL_NUMBER = 1001;

    /**
     * Generates the next sequential unique candidate code from database.
     */
    public static synchronized String generateNextCode() {
        int nextNum = INITIAL_NUMBER;
        String query = "SELECT MAX(id) FROM candidates";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int maxId = rs.getInt(1);
                if (maxId > 0) {
                    nextNum = INITIAL_NUMBER + maxId;
                }
            }
        } catch (Exception e) {
            // Fallback using current timestamp modulo
            nextNum = (int) (1000 + (System.currentTimeMillis() % 8999));
        }

        return PREFIX + nextNum;
    }
}
