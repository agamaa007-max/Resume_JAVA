package com.blindhire;

import com.blindhire.config.AppConfig;
import com.blindhire.database.DatabaseInitializer;
import com.blindhire.server.HttpServerManager;

/**
 * Main application entry point for BlindHire.
 * Blind Resume Screening & Applicant Tracking System.
 */
public class Main {

    public static void main(String[] args) {
        printBanner();

        // 1. Initialize Database Schema and seed data
        System.out.println("[BOOT] Initializing database layer...");
        boolean dbOk = DatabaseInitializer.initialize();
        if (!dbOk) {
            System.err.println("[BOOT] Warning: Could not fully initialize MySQL database.");
            System.err.println("[BOOT] Please ensure MySQL is running on " + AppConfig.getDbHost() + ":" + AppConfig.getDbPort());
            System.err.println("[BOOT] Check config.properties for host, port, user and password settings.");
        } else {
            System.out.println("[BOOT] Database initialization confirmed.");
        }

        // 2. Start Built-in HTTP Server
        try {
            System.out.println("[BOOT] Starting Java HTTP Server...");
            HttpServerManager server = new HttpServerManager();
            server.start();

            System.out.println("\nReady for candidate evaluation:");
            System.out.println("-> Open your browser at: http://localhost:" + AppConfig.getServerPort());
            System.out.println("-> Default recruiter login: recruiter@blindhire.com / Password@123\n");
        } catch (Exception e) {
            System.err.println("[FATAL] Could not start HTTP server on port " + AppConfig.getServerPort() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printBanner() {
        System.out.println("==================================================================");
        System.out.println("      ____  _ _           _ _   _ _            ");
        System.out.println("     |  _ \\| (_)_ __   __| | | | (_)_ __ ___  ");
        System.out.println("     | |_) | | | '_ \\ / _` | |_| | | '__/ _ \\ ");
        System.out.println("     |  _ <| | | | | | (_| |  _  | | | |  __/ ");
        System.out.println("     |_| \\_\\_|_|_| |_|\\__,_|_| |_|_|_|  \\___| ");
        System.out.println("                                               ");
        System.out.println("  Blind Resume Screening & Applicant Tracking System (Core Java & MySQL)");
        System.out.println("==================================================================");
    }
}
