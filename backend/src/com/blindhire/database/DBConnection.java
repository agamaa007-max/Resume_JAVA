package com.blindhire.database;

import com.blindhire.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection provides JDBC database connectivity to MySQL.
 * 
 * Demonstrates:
 * - JDBC Driver loading (com.mysql.cj.jdbc.Driver)
 * - Safe connection creation using DriverManager
 * - Diagnostic feedback for configuration and server availability
 */
public class DBConnection {

    private static boolean driverLoaded = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
            System.out.println("[DB] MySQL JDBC Driver registered successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] ERROR: MySQL Connector/J driver not found in classpath!");
            System.err.println("[DB] Please ensure mysql-connector-j-*.jar is in backend/lib/");
        }
    }

    /**
     * Obtains a new connection to the 'blindhire' database.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            throw new SQLException("MySQL JDBC Driver is not loaded. Check backend/lib dependencies.");
        }
        return DriverManager.getConnection(
            AppConfig.getDbUrl(),
            AppConfig.getDbUser(),
            AppConfig.getDbPassword()
        );
    }

    /**
     * Obtains a connection to the MySQL server without selecting a specific database.
     * Used by DatabaseInitializer to create the database if missing.
     */
    public static Connection getServerConnection() throws SQLException {
        if (!driverLoaded) {
            throw new SQLException("MySQL JDBC Driver is not loaded.");
        }
        return DriverManager.getConnection(
            AppConfig.getDbServerUrl(),
            AppConfig.getDbUser(),
            AppConfig.getDbPassword()
        );
    }

    /**
     * Tests whether a database connection can be established.
     * @return true if successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DB] Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
