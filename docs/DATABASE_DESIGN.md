# BlindHire - Database Architecture & Design Document

## 1. Overview and Design Principles

BlindHire employs a relational MySQL database designed specifically around the principle of **Identity Quarantine**:
1. **Separation of Concerns**: Personally Identifiable Information (PII) such as candidate full names, phone numbers, email addresses, and residential addresses are stored in a dedicated `candidate_identity` table, physically isolated from the `candidates`, `candidate_skills`, `candidate_education`, `candidate_experience`, and `screening_results` tables.
2. **Deterministic Foreign Keys**: All candidate child tables reference `candidates(id)` with cascading deletes (`ON DELETE CASCADE`) to preserve referential integrity without orphan records.
3. **Audit Immutability**: The `audit_logs` table maintains a tamper-evident log of all identity reveals, actions, timestamps, and justification reasons.

---

## 2. Entity-Relationship (ER) Overview

```text
  [users] 1 --------< N [jobs] 1 --------< N [job_skills]
     |                     |
     |                     | 1
     |                     |
     |                     v N
     |              [screening_results]
     |                     ^ N
     |                     |
     |                     | 1
  [audit_logs] >-------- [candidates] 1 -------- 1 [candidate_identity] (QUARANTINED)
                           |
                           +--------< N [candidate_skills]
                           +--------< N [candidate_education]
                           +--------< N [candidate_experience]
                           +--------< N [candidate_projects]
                           +--------< N [resumes]
```

---

## 3. Detailed Table Specifications

### 3.1. `users` (Recruiters & Administrators)
Stores authenticated recruiters. Plain-text passwords are never stored; PBKDF2WithHmacSHA256 hashes and per-user salts are used.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique recruiter identifier |
| `full_name` | VARCHAR(100) | NOT NULL | Recruiter's full name |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE, INDEX | Login email address |
| `password_hash` | VARCHAR(256) | NOT NULL | 65,536 iteration PBKDF2 hash |
| `password_salt` | VARCHAR(256) | NOT NULL | Cryptographic 16-byte random salt |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration timestamp |

### 3.2. `jobs` (Job Postings)
Represents job vacancies posted by recruiters.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique job opening identifier |
| `title` | VARCHAR(150) | NOT NULL | Position title |
| `description` | TEXT | NOT NULL | Full role responsibilities |
| `minimum_education` | VARCHAR(100) | NULL | Educational benchmark |
| `minimum_experience`| INT | DEFAULT 0 | Required years of experience |
| `created_by` | INT | FOREIGN KEY -> users(id) | Recruiter who created the job |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE', INDEX | Vacancy status ('ACTIVE', 'CLOSED') |

### 3.3. `job_skills` (Target Skill Requirements)
Skills defined for a job, segmented into REQUIRED and PREFERRED.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique skill requirement ID |
| `job_id` | INT | FOREIGN KEY -> jobs(id) | Associated job opening |
| `skill_name` | VARCHAR(100) | NOT NULL | Canonical skill name |
| `skill_type` | VARCHAR(20) | NOT NULL DEFAULT 'REQUIRED' | Skill priority ('REQUIRED', 'PREFERRED') |

### 3.4. `candidates` (Anonymous Profiles)
The central candidate entity. Exposes only an anonymous identifier (`candidate_code`).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Surrogate candidate key |
| `candidate_code` | VARCHAR(50) | NOT NULL, UNIQUE, INDEX | Synthetic public ID (e.g. CAND-1001) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Submission timestamp |
| `status` | VARCHAR(20) | DEFAULT 'APPLIED' | Pipeline status ('APPLIED', 'SCREENED', 'SHORTLISTED') |

### 3.5. `candidate_identity` (Quarantined PII)
Separated personal identity information. Inaccessible through ordinary candidate APIs.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Identifier |
| `candidate_id` | INT | NOT NULL, UNIQUE, FK -> candidates(id) | Target candidate |
| `name` | VARCHAR(100) | NOT NULL | Candidate legal name |
| `email` | VARCHAR(100) | NOT NULL | Candidate personal email |
| `phone` | VARCHAR(30) | NULL | Candidate telephone contact |
| `address` | TEXT | NULL | Candidate physical location |

### 3.6. `candidate_skills`
Extracted and normalized technical skills.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Skill record ID |
| `candidate_id` | INT | FOREIGN KEY -> candidates(id) | Candidate owner |
| `skill_name` | VARCHAR(100) | NOT NULL, INDEX | Normalized skill name |

### 3.7. `candidate_education`
Educational qualifications extracted from resume.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Education entry ID |
| `candidate_id` | INT | FOREIGN KEY -> candidates(id) | Candidate owner |
| `degree` | VARCHAR(100) | NULL | Degree obtained (e.g. B.Tech, MCA) |
| `institution` | VARCHAR(150) | NULL | University / Institute |
| `field` | VARCHAR(100) | NULL | Field of study / Specialization |
| `graduation_year` | INT | NULL | Year of completion |
| `percentage` | DOUBLE | NULL | Academic performance percentage / GPA |

### 3.8. `candidate_experience`
Professional work history records.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Experience record ID |
| `candidate_id` | INT | FOREIGN KEY -> candidates(id) | Candidate owner |
| `company` | VARCHAR(150) | NULL | Employer organization |
| `job_title` | VARCHAR(100) | NULL | Position held |
| `years_of_experience` | DOUBLE | DEFAULT 0.0 | Duration in years |
| `description` | TEXT | NULL | Role duties and accomplishments |

### 3.9. `candidate_projects`
Technical or academic projects.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Project record ID |
| `candidate_id` | INT | FOREIGN KEY -> candidates(id) | Candidate owner |
| `project_name` | VARCHAR(150) | NULL | Project title |
| `description` | TEXT | NULL | Technical summary |
| `technologies` | TEXT | NULL | Tools and languages utilized |

### 3.10. `resumes`
Uploaded resume document files and raw extracted text.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Resume document ID |
| `candidate_id` | INT | FOREIGN KEY -> candidates(id) | Candidate owner |
| `file_name` | VARCHAR(255) | NOT NULL | Original uploaded filename |
| `file_path` | VARCHAR(500) | NOT NULL | Local storage path on server |
| `file_type` | VARCHAR(50) | NOT NULL | File format ('PDF', 'DOCX', 'TXT') |
| `extracted_text` | LONGTEXT | NULL | Full extracted document text |
| `uploaded_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Ingestion timestamp |

### 3.11. `screening_results`
Calculated explainable matching scores between a candidate and a specific job opening.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Result ID |
| `candidate_id` | INT | FOREIGN KEY -> candidates(id) | Candidate evaluated |
| `job_id` | INT | FOREIGN KEY -> jobs(id) | Target job evaluated against |
| `skill_score` | DOUBLE | DEFAULT 0.0 | Skill component score (0-100) |
| `experience_score` | DOUBLE | DEFAULT 0.0 | Experience component score (0-100) |
| `education_score` | DOUBLE | DEFAULT 0.0 | Education component score (0-100) |
| `project_score` | DOUBLE | DEFAULT 0.0 | Project component score (0-100) |
| `final_score` | DOUBLE | DEFAULT 0.0 | Weighted overall score (0-100) |
| `screened_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Evaluation timestamp |

### 3.12. `audit_logs`
Tamper-evident compliance trail.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Log entry ID |
| `user_id` | INT | FOREIGN KEY -> users(id) ON DELETE SET NULL | Recruiter performing action |
| `action` | VARCHAR(50) | NOT NULL | Action code (e.g. 'IDENTITY_REVEAL') |
| `candidate_id` | INT | FOREIGN KEY -> candidates(id) ON DELETE SET NULL | Affected candidate |
| `description` | TEXT | NULL | Details and mandatory justification |
| `timestamp` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Log entry timestamp |
