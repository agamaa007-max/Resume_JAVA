package com.blindhire.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PasswordUtil provides secure password hashing and verification
 * using the industry-standard PBKDF2WithHmacSHA256 algorithm.
 * 
 * Demonstrates:
 * - Cryptographic security with per-user salt
 * - Protection against rainbow table attacks
 * - Constant-time comparison to prevent timing attacks
 */
public class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final int SALT_LENGTH = 16;  // bytes

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a cryptographically strong random salt.
     * @return Base64-encoded salt string
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a plain-text password using the supplied salt and PBKDF2.
     * 
     * @param password Plain text password
     * @param saltBase64 Base64 encoded salt
     * @return Base64 encoded hash
     */
    public static String hashPassword(String password, String saltBase64) {
        if (password == null || saltBase64 == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password with " + ALGORITHM, e);
        }
    }

    /**
     * Verifies whether a plain-text password matches a stored hash given the user's salt.
     * 
     * @param password Plain-text candidate password
     * @param saltBase64 Stored salt
     * @param expectedHash Stored hash
     * @return true if valid, false otherwise
     */
    public static boolean verifyPassword(String password, String saltBase64, String expectedHash) {
        if (password == null || saltBase64 == null || expectedHash == null) {
            return false;
        }
        String computedHash = hashPassword(password, saltBase64);
        return slowEquals(computedHash.getBytes(), expectedHash.getBytes());
    }

    /**
     * Constant-time comparison to protect against side-channel timing attacks.
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
