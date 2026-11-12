package com.blindhire.service;

import com.blindhire.dao.UserDAO;
import com.blindhire.model.User;
import com.blindhire.security.PasswordUtil;
import com.blindhire.security.SessionManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthService coordinates recruiter registration, login validation,
 * session minting, and profile management.
 */
public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Map<String, Object> register(String fullName, String email, String password) throws Exception {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email address is required.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        // Check duplicate email
        User existing = userDAO.findUserByEmail(email.trim().toLowerCase());
        if (existing != null) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(hash);
        user.setPasswordSalt(salt);

        User savedUser = userDAO.registerUser(user);
        String token = SessionManager.createSession(savedUser.getId(), savedUser.getEmail(), savedUser.getFullName());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", savedUser.getId());
        result.put("fullName", savedUser.getFullName());
        result.put("email", savedUser.getEmail());
        return result;
    }

    public Map<String, Object> login(String email, String password) throws Exception {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        User user = userDAO.findUserByEmail(email.trim().toLowerCase());
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        boolean valid = PasswordUtil.verifyPassword(password, user.getPasswordSalt(), user.getPasswordHash());
        if (!valid) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = SessionManager.createSession(user.getId(), user.getEmail(), user.getFullName());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("fullName", user.getFullName());
        result.put("email", user.getEmail());
        return result;
    }

    public User getProfile(int userId) throws SQLException {
        User user = userDAO.findUserById(userId);
        if (user != null) {
            // Never expose password credentials
            user.setPasswordHash(null);
            user.setPasswordSalt(null);
        }
        return user;
    }

    public boolean changePassword(int userId, String currentPassword, String newPassword) throws Exception {
        User user = userDAO.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordSalt(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }

        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hashPassword(newPassword, newSalt);

        return userDAO.updatePassword(userId, newHash, newSalt);
    }
}
