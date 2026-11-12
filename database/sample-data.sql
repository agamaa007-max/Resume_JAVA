-- ==========================================================
-- BlindHire Sample Synthetic Test Data
-- Pre-hashed password for all 3 recruiters: "Password@123"
-- ==========================================================

USE blindhire;

-- 1. Recruiters
-- Passwords are generated via PBKDF2WithHmacSHA256, 65536 iterations
INSERT INTO users (id, full_name, email, password_hash, password_salt) VALUES
(1, 'Alex Morgan', 'recruiter@blindhire.com', 'lK7K0K5jDqu4U9tHZZJ8XzC8t3qQk9m2eT0Yx1Vb7wU=', 'YWJjZGVmZ2hpamtsbW5vcA=='),
(2, 'Sarah Jenkins', 'sarah.hr@blindhire.com', 'lK7K0K5jDqu4U9tHZZJ8XzC8t3qQk9m2eT0Yx1Vb7wU=', 'YWJjZGVmZ2hpamtsbW5vcA=='),
(3, 'David Chen', 'david.talent@blindhire.com', 'lK7K0K5jDqu4U9tHZZJ8XzC8t3qQk9m2eT0Yx1Vb7wU=', 'YWJjZGVmZ2hpamtsbW5vcA==')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 2. Job Postings
INSERT INTO jobs (id, title, description, minimum_education, minimum_experience, created_by, status) VALUES
(1, 'Java Backend Developer', 'Seeking a passionate Java developer with experience in building scalable REST APIs, relational databases (SQL/MySQL), and clean software design.', 'B.Tech / B.E / MCA', 1, 1, 'ACTIVE'),
(2, 'Frontend Web Developer', 'Looking for a UI developer proficient in modern HTML5, CSS3, responsive layouts, vanilla JavaScript, and Git workflows.', 'Bachelor Degree', 1, 1, 'ACTIVE'),
(3, 'Full Stack Java Engineer', 'Responsible for end-to-end web application engineering combining Core Java backend services with modern responsive frontend interfaces.', 'B.Tech Computer Science', 2, 2, 'ACTIVE')
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 3. Job Skills
INSERT INTO job_skills (job_id, skill_name, skill_type) VALUES
(1, 'Java', 'REQUIRED'), (1, 'SQL', 'REQUIRED'), (1, 'Spring Boot', 'REQUIRED'), (1, 'Git', 'REQUIRED'),
(1, 'Docker', 'PREFERRED'), (1, 'AWS', 'PREFERRED'),
(2, 'HTML', 'REQUIRED'), (2, 'CSS', 'REQUIRED'), (2, 'JavaScript', 'REQUIRED'), (2, 'Git', 'REQUIRED'),
(2, 'React', 'PREFERRED'), (2, 'TypeScript', 'PREFERRED'),
(3, 'Java', 'REQUIRED'), (3, 'JavaScript', 'REQUIRED'), (3, 'SQL', 'REQUIRED'), (3, 'HTML', 'REQUIRED'), (3, 'CSS', 'REQUIRED'),
(3, 'Docker', 'PREFERRED'), (3, 'Git', 'PREFERRED');

-- 4. Anonymous Candidates
INSERT INTO candidates (id, candidate_code, status) VALUES
(1, 'CAND-1001', 'SCREENED'),
(2, 'CAND-1002', 'SCREENED'),
(3, 'CAND-1003', 'SCREENED'),
(4, 'CAND-1004', 'APPLIED'),
(5, 'CAND-1005', 'APPLIED')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 5. Candidate Identity (Stored separately)
INSERT INTO candidate_identity (candidate_id, name, email, phone, address) VALUES
(1, 'Rahul Sharma', 'rahul.sharma@example.com', '+91 9876543210', 'New Delhi, India'),
(2, 'Priya Patel', 'priya.patel@example.com', '+91 9876543211', 'Bangalore, India'),
(3, 'Amit Verma', 'amit.verma@example.com', '+91 9876543212', 'Pune, India'),
(4, 'Sneha Kulkarni', 'sneha.k@example.com', '+91 9876543213', 'Hyderabad, India'),
(5, 'Rohan Mehta', 'rohan.m@example.com', '+91 9876543214', 'Mumbai, India')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 6. Candidate Skills
INSERT INTO candidate_skills (candidate_id, skill_name) VALUES
(1, 'Java'), (1, 'SQL'), (1, 'Spring Boot'), (1, 'Git'), (1, 'Python'), (1, 'MySQL'),
(2, 'Python'), (2, 'SQL'), (2, 'Machine Learning'), (2, 'Git'), (2, 'Excel'),
(3, 'HTML'), (3, 'CSS'), (3, 'JavaScript'), (3, 'Java'), (3, 'SQL'), (3, 'Git'),
(4, 'Java'), (4, 'C++'), (4, 'SQL'), (4, 'Docker'), (4, 'AWS'),
(5, 'HTML'), (5, 'CSS'), (5, 'JavaScript'), (5, 'Excel');

-- 7. Candidate Education
INSERT INTO candidate_education (candidate_id, degree, institution, field, graduation_year, percentage) VALUES
(1, 'B.Tech', 'Delhi Technological University', 'Computer Science & Engineering', 2024, 84.5),
(2, 'BCA', 'Bangalore University', 'Computer Applications', 2023, 78.0),
(3, 'B.Tech', 'Pune Institute of Computer Technology', 'Information Technology', 2023, 82.0),
(4, 'M.Tech', 'IIIT Hyderabad', 'Computer Science', 2022, 88.0),
(5, 'B.Sc', 'Mumbai University', 'Computer Science', 2025, 75.0);

-- 8. Candidate Experience
INSERT INTO candidate_experience (candidate_id, company, job_title, years_of_experience, description) VALUES
(1, 'FinTech Solutions', 'Junior Java Engineer', 2.0, 'Developed backend microservices, optimized database queries in MySQL, and integrated payment gateways.'),
(2, 'Data Insights Lab', 'Associate Data Analyst', 1.0, 'Analyzed tabular datasets, automated SQL reports, and generated Python data processing scripts.'),
(3, 'Creative Tech Works', 'Web Developer', 2.5, 'Built responsive user interfaces with JavaScript and CSS, and developed RESTful API endpoints in Java.'),
(4, 'CloudScale Systems', 'Software Engineer', 4.0, 'Architected containerized microservices using Docker, Java, and AWS infrastructure.'),
(5, 'Academic Projects', 'Intern', 0.5, 'Assisted in building academic departmental portal using HTML, CSS, and basic JavaScript.');

-- 9. Candidate Projects
INSERT INTO candidate_projects (candidate_id, project_name, description, technologies) VALUES
(1, 'Banking Management System', 'Secure online banking system with transaction ledgers and authentication.', 'Java, MySQL, JDBC, Servlets'),
(1, 'Resume Screening System', 'Prototype screening engine comparing keyword matches.', 'Java, Regex, SQL'),
(2, 'Predictive Analytics Dashboard', 'Visualizes sales forecasting and trend models.', 'Python, Pandas, SQL, Excel'),
(3, 'E-Commerce Web Portal', 'Full responsive storefront with cart management.', 'HTML, CSS, JavaScript, Java REST'),
(4, 'Distributed Microservices Gateway', 'High-throughput routing and load-balancing gateway.', 'Java, Docker, AWS'),
(5, 'Student Record System', 'Simple CRUD application for student records.', 'HTML, CSS, JavaScript');

-- 10. Screening Results
INSERT INTO screening_results (candidate_id, job_id, skill_score, experience_score, education_score, project_score, final_score) VALUES
(1, 1, 95.0, 100.0, 95.0, 90.0, 95.75),
(2, 1, 40.0, 80.0, 70.0, 60.0, 56.5),
(3, 1, 75.0, 100.0, 95.0, 85.0, 85.25);

-- 11. Audit Logs
INSERT INTO audit_logs (user_id, action, candidate_id, description) VALUES
(1, 'SYSTEM_INIT', NULL, 'Database initialized with standard schema and initial sample profiles.');
