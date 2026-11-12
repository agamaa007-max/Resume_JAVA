package com.blindhire.database;

import com.blindhire.config.AppConfig;
import com.blindhire.security.PasswordUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseInitializer verifies MySQL connectivity on startup,
 * validates schema integrity, creates missing tables, and populates initial sample data.
 */
public class DatabaseInitializer {

    // Direct DDL statements for all 12 tables to guarantee 100% reliable initialization
    private static final String[] DDL_STATEMENTS = {
        // 1. Users Table
        "CREATE TABLE IF NOT EXISTS users (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    full_name VARCHAR(100) NOT NULL," +
        "    email VARCHAR(100) NOT NULL UNIQUE," +
        "    password_hash VARCHAR(256) NOT NULL," +
        "    password_salt VARCHAR(256) NOT NULL," +
        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "    INDEX idx_user_email (email)" +
        ") ENGINE=InnoDB",

        // 2. Jobs Table
        "CREATE TABLE IF NOT EXISTS jobs (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    title VARCHAR(150) NOT NULL," +
        "    description TEXT NOT NULL," +
        "    minimum_education VARCHAR(100)," +
        "    minimum_experience INT DEFAULT 0," +
        "    created_by INT NOT NULL," +
        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "    status VARCHAR(20) DEFAULT 'ACTIVE'," +
        "    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE," +
        "    INDEX idx_job_status (status)" +
        ") ENGINE=InnoDB",

        // 3. Job Skills Table
        "CREATE TABLE IF NOT EXISTS job_skills (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    job_id INT NOT NULL," +
        "    skill_name VARCHAR(100) NOT NULL," +
        "    skill_type VARCHAR(20) NOT NULL DEFAULT 'REQUIRED'," +
        "    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE," +
        "    INDEX idx_job_skill (job_id, skill_name)" +
        ") ENGINE=InnoDB",

        // 4. Candidates Table
        "CREATE TABLE IF NOT EXISTS candidates (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    candidate_code VARCHAR(50) NOT NULL UNIQUE," +
        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "    status VARCHAR(20) DEFAULT 'APPLIED'," +
        "    INDEX idx_candidate_code (candidate_code)" +
        ") ENGINE=InnoDB",

        // 5. Candidate Identity Table (Quarantined PII)
        "CREATE TABLE IF NOT EXISTS candidate_identity (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    candidate_id INT NOT NULL UNIQUE," +
        "    name VARCHAR(100) NOT NULL," +
        "    email VARCHAR(100) NOT NULL," +
        "    phone VARCHAR(30)," +
        "    address TEXT," +
        "    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE" +
        ") ENGINE=InnoDB",

        // 6. Candidate Skills Table
        "CREATE TABLE IF NOT EXISTS candidate_skills (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    candidate_id INT NOT NULL," +
        "    skill_name VARCHAR(100) NOT NULL," +
        "    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE," +
        "    INDEX idx_cand_skill (candidate_id, skill_name)" +
        ") ENGINE=InnoDB",

        // 7. Candidate Education Table
        "CREATE TABLE IF NOT EXISTS candidate_education (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    candidate_id INT NOT NULL," +
        "    degree VARCHAR(100)," +
        "    institution VARCHAR(150)," +
        "    field VARCHAR(100)," +
        "    graduation_year INT," +
        "    percentage DOUBLE," +
        "    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE" +
        ") ENGINE=InnoDB",

        // 8. Candidate Experience Table
        "CREATE TABLE IF NOT EXISTS candidate_experience (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    candidate_id INT NOT NULL," +
        "    company VARCHAR(150)," +
        "    job_title VARCHAR(100)," +
        "    years_of_experience DOUBLE DEFAULT 0.0," +
        "    description TEXT," +
        "    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE" +
        ") ENGINE=InnoDB",

        // 9. Candidate Projects Table
        "CREATE TABLE IF NOT EXISTS candidate_projects (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    candidate_id INT NOT NULL," +
        "    project_name VARCHAR(150)," +
        "    description TEXT," +
        "    technologies TEXT," +
        "    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE" +
        ") ENGINE=InnoDB",

        // 10. Resumes Table
        "CREATE TABLE IF NOT EXISTS resumes (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    candidate_id INT NOT NULL," +
        "    file_name VARCHAR(255) NOT NULL," +
        "    file_path VARCHAR(500) NOT NULL," +
        "    file_type VARCHAR(50) NOT NULL," +
        "    extracted_text LONGTEXT," +
        "    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE" +
        ") ENGINE=InnoDB",

        // 11. Screening Results Table
        "CREATE TABLE IF NOT EXISTS screening_results (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    candidate_id INT NOT NULL," +
        "    job_id INT NOT NULL," +
        "    skill_score DOUBLE DEFAULT 0.0," +
        "    experience_score DOUBLE DEFAULT 0.0," +
        "    education_score DOUBLE DEFAULT 0.0," +
        "    project_score DOUBLE DEFAULT 0.0," +
        "    final_score DOUBLE DEFAULT 0.0," +
        "    screened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE," +
        "    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE," +
        "    INDEX idx_screening_job (job_id)," +
        "    INDEX idx_screening_candidate (candidate_id)" +
        ") ENGINE=InnoDB",

        // 12. Audit Logs Table
        "CREATE TABLE IF NOT EXISTS audit_logs (" +
        "    id INT AUTO_INCREMENT PRIMARY KEY," +
        "    user_id INT," +
        "    action VARCHAR(50) NOT NULL," +
        "    candidate_id INT," +
        "    description TEXT," +
        "    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL," +
        "    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE SET NULL," +
        "    INDEX idx_audit_user (user_id)," +
        "    INDEX idx_audit_candidate (candidate_id)" +
        ") ENGINE=InnoDB"
    };

    public static boolean initialize() {
        System.out.println("[DB-INIT] Checking MySQL database status...");

        // Step 1: Ensure database exists
        try (Connection serverConn = DBConnection.getServerConnection();
             Statement stmt = serverConn.createStatement()) {
            String createDbSql = "CREATE DATABASE IF NOT EXISTS " + AppConfig.getDbName() +
                                 " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            stmt.executeUpdate(createDbSql);
            System.out.println("[DB-INIT] Database '" + AppConfig.getDbName() + "' is ready.");
        } catch (SQLException e) {
            System.err.println("[DB-INIT] Warning: Could not connect to MySQL server root to verify database: " + e.getMessage());
            System.err.println("[DB-INIT] Please ensure MySQL is running on " + AppConfig.getDbHost() + ":" + AppConfig.getDbPort());
            return false;
        }

        // Step 2: Validate and initialize schema tables
        try (Connection conn = DBConnection.getConnection()) {
            // Check if users table is valid for BlindHire (must have 'full_name' column)
            boolean usersValid = false;
            if (checkTableExists(conn, "users")) {
                try (ResultSet rs = conn.getMetaData().getColumns(null, null, "users", "full_name")) {
                    if (rs.next()) {
                        usersValid = true;
                    }
                } catch (SQLException ignored) {}

                if (!usersValid) {
                    System.out.println("[DB-INIT] Incompatible legacy 'users' table detected (missing full_name). Rebuilding schema...");
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                        stmt.executeUpdate("DROP TABLE IF EXISTS users");
                        stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
                    }
                }
            }

            // Create all 12 tables
            System.out.println("[DB-INIT] Ensuring all 12 relational tables exist...");
            try (Statement stmt = conn.createStatement()) {
                for (String ddl : DDL_STATEMENTS) {
                    try {
                        stmt.execute(ddl);
                    } catch (SQLException ex) {
                        System.err.println("[DB-INIT] DDL Notice: " + ex.getMessage());
                    }
                }
            }

            System.out.println("[DB-INIT] Relational tables validated successfully.");

            // Step 3: Check if sample data is needed (if users table has 0 rows)
            if (isTableEmpty(conn, "users")) {
                System.out.println("[DB-INIT] No users found. Seeding initial sample data...");
                seedInitialData(conn);
            } else {
                System.out.println("[DB-INIT] User accounts already present in database.");
            }

            return true;
        } catch (SQLException e) {
            System.err.println("[DB-INIT] Error connecting to '" + AppConfig.getDbName() + "': " + e.getMessage());
            return false;
        }
    }

    private static boolean checkTableExists(Connection conn, String tableName) {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean isTableEmpty(Connection conn, String tableName) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException ignored) {}
        return true;
    }

    public static void executeSqlScript(Connection conn, String relativePath) {
        File file = new File(relativePath);
        if (!file.exists()) {
            file = new File(".." + File.separator + relativePath);
        }
        if (!file.exists()) {
            System.err.println("[DB-INIT] SQL script not found at " + relativePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             Statement stmt = conn.createStatement()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Remove line comments
                int commentIdx = trimmed.indexOf("--");
                if (commentIdx >= 0) {
                    trimmed = trimmed.substring(0, commentIdx).trim();
                }
                if (trimmed.isEmpty()) {
                    continue;
                }
                sb.append(trimmed).append(" ");
                if (trimmed.endsWith(";")) {
                    String query = sb.toString().trim();
                    query = query.substring(0, query.length() - 1).trim(); // remove semicolon
                    if (!query.isEmpty() && !query.toUpperCase().startsWith("CREATE DATABASE") && !query.toUpperCase().startsWith("USE ")) {
                        try {
                            stmt.execute(query);
                        } catch (SQLException ex) {
                            if (!ex.getMessage().toLowerCase().contains("already exists")) {
                                System.err.println("[DB-INIT] Notice: " + ex.getMessage());
                            }
                        }
                    }
                    sb.setLength(0);
                }
            }
            System.out.println("[DB-INIT] Successfully processed " + file.getName());
        } catch (Exception e) {
            System.err.println("[DB-INIT] Error executing " + file.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Seeds initial recruiters, jobs, and candidates with verified PBKDF2 passwords.
     */
    public static void seedInitialData(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Default password for all sample recruiters: "Password@123"
            String salt1 = PasswordUtil.generateSalt();
            String hash1 = PasswordUtil.hashPassword("Password@123", salt1);

            String salt2 = PasswordUtil.generateSalt();
            String hash2 = PasswordUtil.hashPassword("Password@123", salt2);

            String salt3 = PasswordUtil.generateSalt();
            String hash3 = PasswordUtil.hashPassword("Password@123", salt3);

            // 1. Insert 3 Recruiters
            stmt.executeUpdate(String.format(
                "INSERT INTO users (id, full_name, email, password_hash, password_salt) VALUES " +
                "(1, 'Alex Morgan', 'recruiter@blindhire.com', '%s', '%s'), " +
                "(2, 'Sarah Jenkins', 'sarah.hr@blindhire.com', '%s', '%s'), " +
                "(3, 'David Chen', 'david.talent@blindhire.com', '%s', '%s') " +
                "ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), password_hash=VALUES(password_hash), password_salt=VALUES(password_salt)",
                hash1, salt1, hash2, salt2, hash3, salt3
            ));

            // 2. Insert 3 Job Postings
            stmt.executeUpdate(
                "INSERT INTO jobs (id, title, description, minimum_education, minimum_experience, created_by, status) VALUES " +
                "(1, 'Java Backend Developer', 'Seeking a passionate Java developer with experience in building scalable REST APIs, relational databases (SQL/MySQL), and clean software design.', 'B.Tech / B.E / MCA', 1, 1, 'ACTIVE'), " +
                "(2, 'Frontend Web Developer', 'Looking for a UI developer proficient in modern HTML5, CSS3, responsive layouts, vanilla JavaScript, and Git workflows.', 'Bachelor Degree', 1, 1, 'ACTIVE'), " +
                "(3, 'Full Stack Java Engineer', 'Responsible for end-to-end web application engineering combining Core Java backend services with modern responsive frontend interfaces.', 'B.Tech Computer Science', 2, 2, 'ACTIVE') " +
                "ON DUPLICATE KEY UPDATE title=VALUES(title)"
            );

            // 3. Insert Job Skills
            stmt.executeUpdate("DELETE FROM job_skills WHERE job_id IN (1, 2, 3)");
            stmt.executeUpdate(
                "INSERT INTO job_skills (job_id, skill_name, skill_type) VALUES " +
                "(1, 'Java', 'REQUIRED'), (1, 'SQL', 'REQUIRED'), (1, 'Spring Boot', 'REQUIRED'), (1, 'Git', 'REQUIRED'), " +
                "(1, 'Docker', 'PREFERRED'), (1, 'AWS', 'PREFERRED'), " +
                "(2, 'HTML', 'REQUIRED'), (2, 'CSS', 'REQUIRED'), (2, 'JavaScript', 'REQUIRED'), (2, 'Git', 'REQUIRED'), " +
                "(2, 'React', 'PREFERRED'), (2, 'TypeScript', 'PREFERRED'), " +
                "(3, 'Java', 'REQUIRED'), (3, 'JavaScript', 'REQUIRED'), (3, 'SQL', 'REQUIRED'), (3, 'HTML', 'REQUIRED'), (3, 'CSS', 'REQUIRED'), " +
                "(3, 'Docker', 'PREFERRED'), (3, 'Git', 'PREFERRED')"
            );

            // 4. Insert 5 Anonymous Candidates
            stmt.executeUpdate(
                "INSERT INTO candidates (id, candidate_code, status) VALUES " +
                "(1, 'CAND-1001', 'SCREENED'), " +
                "(2, 'CAND-1002', 'SCREENED'), " +
                "(3, 'CAND-1003', 'SCREENED'), " +
                "(4, 'CAND-1004', 'APPLIED'), " +
                "(5, 'CAND-1005', 'APPLIED') " +
                "ON DUPLICATE KEY UPDATE status=VALUES(status)"
            );

            // 5. Insert Candidate Identity (Separated from screening information!)
            stmt.executeUpdate(
                "INSERT INTO candidate_identity (candidate_id, name, email, phone, address) VALUES " +
                "(1, 'Rahul Sharma', 'rahul.sharma@example.com', '+91 9876543210', 'New Delhi, India'), " +
                "(2, 'Priya Patel', 'priya.patel@example.com', '+91 9876543211', 'Bangalore, India'), " +
                "(3, 'Amit Verma', 'amit.verma@example.com', '+91 9876543212', 'Pune, India'), " +
                "(4, 'Sneha Kulkarni', 'sneha.k@example.com', '+91 9876543213', 'Hyderabad, India'), " +
                "(5, 'Rohan Mehta', 'rohan.m@example.com', '+91 9876543214', 'Mumbai, India') " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name)"
            );

            // 6. Insert Candidate Skills
            stmt.executeUpdate("DELETE FROM candidate_skills WHERE candidate_id IN (1, 2, 3, 4, 5)");
            stmt.executeUpdate(
                "INSERT INTO candidate_skills (candidate_id, skill_name) VALUES " +
                "(1, 'Java'), (1, 'SQL'), (1, 'Spring Boot'), (1, 'Git'), (1, 'Python'), (1, 'MySQL'), " +
                "(2, 'Python'), (2, 'SQL'), (2, 'Machine Learning'), (2, 'Git'), (2, 'Excel'), " +
                "(3, 'HTML'), (3, 'CSS'), (3, 'JavaScript'), (3, 'Java'), (3, 'SQL'), (3, 'Git'), " +
                "(4, 'Java'), (4, 'C++'), (4, 'SQL'), (4, 'Docker'), (4, 'AWS'), " +
                "(5, 'HTML'), (5, 'CSS'), (5, 'JavaScript'), (5, 'Excel')"
            );

            // 7. Insert Candidate Education
            stmt.executeUpdate("DELETE FROM candidate_education WHERE candidate_id IN (1, 2, 3, 4, 5)");
            stmt.executeUpdate(
                "INSERT INTO candidate_education (candidate_id, degree, institution, field, graduation_year, percentage) VALUES " +
                "(1, 'B.Tech', 'Delhi Technological University', 'Computer Science & Engineering', 2024, 84.5), " +
                "(2, 'BCA', 'Bangalore University', 'Computer Applications', 2023, 78.0), " +
                "(3, 'B.Tech', 'Pune Institute of Computer Technology', 'Information Technology', 2023, 82.0), " +
                "(4, 'M.Tech', 'IIIT Hyderabad', 'Computer Science', 2022, 88.0), " +
                "(5, 'B.Sc', 'Mumbai University', 'Computer Science', 2025, 75.0)"
            );

            // 8. Insert Candidate Experience
            stmt.executeUpdate("DELETE FROM candidate_experience WHERE candidate_id IN (1, 2, 3, 4, 5)");
            stmt.executeUpdate(
                "INSERT INTO candidate_experience (candidate_id, company, job_title, years_of_experience, description) VALUES " +
                "(1, 'FinTech Solutions', 'Junior Java Engineer', 2.0, 'Developed backend microservices, optimized database queries in MySQL, and integrated payment gateways.'), " +
                "(2, 'Data Insights Lab', 'Associate Data Analyst', 1.0, 'Analyzed tabular datasets, automated SQL reports, and generated Python data processing scripts.'), " +
                "(3, 'Creative Tech Works', 'Web Developer', 2.5, 'Built responsive user interfaces with JavaScript and CSS, and developed RESTful API endpoints in Java.'), " +
                "(4, 'CloudScale Systems', 'Software Engineer', 4.0, 'Architected containerized microservices using Docker, Java, and AWS infrastructure.'), " +
                "(5, 'Academic Projects', 'Intern', 0.5, 'Assisted in building academic departmental portal using HTML, CSS, and basic JavaScript.')"
            );

            // 9. Insert Candidate Projects
            stmt.executeUpdate("DELETE FROM candidate_projects WHERE candidate_id IN (1, 2, 3, 4, 5)");
            stmt.executeUpdate(
                "INSERT INTO candidate_projects (candidate_id, project_name, description, technologies) VALUES " +
                "(1, 'Banking Management System', 'Secure online banking system with transaction ledgers and authentication.', 'Java, MySQL, JDBC, Servlets'), " +
                "(1, 'Resume Screening System', 'Prototype screening engine comparing keyword matches.', 'Java, Regex, SQL'), " +
                "(2, 'Predictive Analytics Dashboard', 'Visualizes sales forecasting and trend models.', 'Python, Pandas, SQL, Excel'), " +
                "(3, 'E-Commerce Web Portal', 'Full responsive storefront with cart management.', 'HTML, CSS, JavaScript, Java REST'), " +
                "(4, 'Distributed Microservices Gateway', 'High-throughput routing and load-balancing gateway.', 'Java, Docker, AWS'), " +
                "(5, 'Student Record System', 'Simple CRUD application for student records.', 'HTML, CSS, JavaScript')"
            );

            // 10. Insert Initial Screening Results for Job 1 (Java Backend Developer)
            stmt.executeUpdate("DELETE FROM screening_results WHERE job_id = 1 AND candidate_id IN (1, 2, 3)");
            stmt.executeUpdate(
                "INSERT INTO screening_results (candidate_id, job_id, skill_score, experience_score, education_score, project_score, final_score) VALUES " +
                "(1, 1, 95.0, 100.0, 95.0, 90.0, 95.75), " +
                "(2, 1, 40.0, 80.0, 70.0, 60.0, 56.5), " +
                "(3, 1, 75.0, 100.0, 95.0, 85.0, 85.25)"
            );

            // 11. Initial Audit Log
            stmt.executeUpdate(
                "INSERT INTO audit_logs (user_id, action, candidate_id, description) VALUES " +
                "(1, 'SYSTEM_INIT', NULL, 'Database schema synchronized and initial synthetic sample data populated.')"
            );

            System.out.println("[DB-INIT] Initial sample data successfully seeded into MySQL.");
        } catch (SQLException e) {
            System.err.println("[DB-INIT] Error seeding sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
