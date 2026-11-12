-- ==========================================================
-- BlindHire Database Schema
-- Blind Resume Screening & Applicant Tracking System
-- ==========================================================

CREATE DATABASE IF NOT EXISTS blindhire CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blindhire;

-- 1. Users (Recruiters and Administrators)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    password_salt VARCHAR(256) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB;

-- 2. Jobs Postings
CREATE TABLE IF NOT EXISTS jobs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    minimum_education VARCHAR(100),
    minimum_experience INT DEFAULT 0,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_job_status (status)
) ENGINE=InnoDB;

-- 3. Job Skills (Required and Preferred)
CREATE TABLE IF NOT EXISTS job_skills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    job_id INT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    skill_type VARCHAR(20) NOT NULL DEFAULT 'REQUIRED', -- REQUIRED, PREFERRED
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    INDEX idx_job_skill (job_id, skill_name)
) ENGINE=InnoDB;

-- 4. Candidates (Anonymous Profiles)
CREATE TABLE IF NOT EXISTS candidates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_code VARCHAR(50) NOT NULL UNIQUE, -- e.g. CAND-1001
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'APPLIED',
    INDEX idx_candidate_code (candidate_code)
) ENGINE=InnoDB;

-- 5. Candidate Identity (Stored separately for blind evaluation)
CREATE TABLE IF NOT EXISTS candidate_identity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    address TEXT,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Candidate Skills
CREATE TABLE IF NOT EXISTS candidate_skills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    INDEX idx_cand_skill (candidate_id, skill_name)
) ENGINE=InnoDB;

-- 7. Candidate Education
CREATE TABLE IF NOT EXISTS candidate_education (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    degree VARCHAR(100),
    institution VARCHAR(150),
    field VARCHAR(100),
    graduation_year INT,
    percentage DOUBLE,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Candidate Experience
CREATE TABLE IF NOT EXISTS candidate_experience (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    company VARCHAR(150),
    job_title VARCHAR(100),
    years_of_experience DOUBLE DEFAULT 0.0,
    description TEXT,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. Candidate Projects
CREATE TABLE IF NOT EXISTS candidate_projects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    project_name VARCHAR(150),
    description TEXT,
    technologies TEXT,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Resumes
CREATE TABLE IF NOT EXISTS resumes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    extracted_text LONGTEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. Screening Results
CREATE TABLE IF NOT EXISTS screening_results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    job_id INT NOT NULL,
    skill_score DOUBLE DEFAULT 0.0,
    experience_score DOUBLE DEFAULT 0.0,
    education_score DOUBLE DEFAULT 0.0,
    project_score DOUBLE DEFAULT 0.0,
    final_score DOUBLE DEFAULT 0.0,
    screened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    INDEX idx_screening_job (job_id),
    INDEX idx_screening_candidate (candidate_id)
) ENGINE=InnoDB;

-- 12. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(50) NOT NULL,
    candidate_id INT,
    description TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE SET NULL,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_candidate (candidate_id)
) ENGINE=InnoDB;
