package com.blindhire.security;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionManager handles in-memory authentication sessions with secure tokens and expiry.
 * 
 * Demonstrates:
 * - Thread-safe ConcurrentHashMap
 * - Cryptographically random session tokens
 * - Expiration and cleanup
 */
public class SessionManager {

    private static final long SESSION_TIMEOUT_MS = 24 * 60 * 60 * 1000L; // 24 hours
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, UserSession> SESSIONS = new ConcurrentHashMap<>();

    public static class UserSession {
        private final int userId;
        private final String email;
        private final String fullName;
        private long lastAccessed;

        public UserSession(int userId, String email, String fullName) {
            this.userId = userId;
            this.email = email;
            this.fullName = fullName;
            this.lastAccessed = System.currentTimeMillis();
        }

        public int getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public long getLastAccessed() { return lastAccessed; }

        public void touch() {
            this.lastAccessed = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return (System.currentTimeMillis() - lastAccessed) > SESSION_TIMEOUT_MS;
        }
    }

    /**
     * Creates a new session for an authenticated user.
     * @return 32-byte secure Base64 token
     */
    public static String createSession(int userId, String email, String fullName) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        UserSession session = new UserSession(userId, email, fullName);
        SESSIONS.put(token, session);
        cleanExpiredSessions();
        return token;
    }

    /**
     * Validates and returns active session for token.
     * Refreshes last accessed timestamp if active.
     */
    public static UserSession getSession(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        UserSession session = SESSIONS.get(token);
        if (session != null) {
            if (session.isExpired()) {
                SESSIONS.remove(token);
                return null;
            }
            session.touch();
        }
        return session;
    }

    /**
     * Logs out by removing the session token.
     */
    public static void invalidateSession(String token) {
        if (token != null) {
            SESSIONS.remove(token);
        }
    }

    private static void cleanExpiredSessions() {
        SESSIONS.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
