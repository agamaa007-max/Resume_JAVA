package com.blindhire.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * AppConfig loads application settings from config.properties or environment variables.
 * Provides fallback defaults so the application can run out-of-the-box.
 */
public class AppConfig {

    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        // Attempt 1: root directory config.properties
        File file = new File("config.properties");
        if (!file.exists()) {
            // Attempt 2: backend/config.properties
            file = new File("backend" + File.separator + "config.properties");
        }

        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
                System.out.println("[CONFIG] Loaded configuration from " + file.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("[CONFIG] Warning: Could not read " + file.getName() + ": " + e.getMessage());
            }
        } else {
            System.out.println("[CONFIG] No config.properties found, using environment variables or defaults.");
        }
    }

    public static String get(String key, String defaultValue) {
        // 1. Check System Properties (-Dkey=value)
        String val = System.getProperty(key);
        if (val != null && !val.trim().isEmpty()) return val.trim();

        // 2. Check Environment Variables (KEY or DB_KEY)
        val = System.getenv(key);
        if (val != null && !val.trim().isEmpty()) return val.trim();
        val = System.getenv(key.replace('.', '_').toUpperCase());
        if (val != null && !val.trim().isEmpty()) return val.trim();

        // 3. Check loaded properties file
        val = properties.getProperty(key);
        if (val != null && !val.trim().isEmpty()) return val.trim();

        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, null);
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    // Database configuration getters
    public static String getDbHost() { return get("db.host", "localhost"); }
    public static int getDbPort() { return getInt("db.port", 3306); }
    public static String getDbName() { return get("db.name", "blindhire"); }
    public static String getDbUser() { return get("db.user", "root"); }
    public static String getDbPassword() { return get("db.password", ""); }

    public static String getDbUrl() {
        return String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
            getDbHost(), getDbPort(), getDbName()
        );
    }

    public static String getDbServerUrl() {
        // Without database name, used to create database if not exists
        return String.format(
            "jdbc:mysql://%s:%d/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
            getDbHost(), getDbPort()
        );
    }

    // Server configuration
    public static int getServerPort() { return getInt("server.port", 8080); }
    public static String getUploadDir() { return get("upload.dir", "uploads" + File.separator + "resumes"); }
}
