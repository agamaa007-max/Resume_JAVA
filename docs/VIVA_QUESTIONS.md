# BlindHire - Comprehensive Academic Viva Questions & Answers

This guide is prepared to help you confidently answer technical examination questions during your college software project demonstration.

---

## Section 1: Java & Object-Oriented Programming (OOP)

### Q1. Why did you choose Core Java instead of Spring Boot or Django for this project?
**Answer**:
BlindHire is designed to demonstrate deep mastery of core computer science and software engineering principles without framework abstraction. Building the application using Core Java (`com.sun.net.httpserver.HttpServer`, JDBC, and standard design patterns) makes every line of logic explicit, easy to explain to examiners, and transparent in performance without heavy memory overhead.

### Q2. How is Encapsulation demonstrated in your codebase?
**Answer**:
All model classes (e.g. `User`, `Job`, `Candidate`, `ScreeningResult`, `Education`) declare their fields as `private`. Access to these fields is strictly controlled via public getter and setter methods. This ensures that internal object states cannot be directly mutated by external classes, maintaining data validation and object integrity.

### Q3. Where have you used Polymorphism and Abstraction?
**Answer**:
In our resume parsing subsystem:
- We created the `ResumeParser` interface declaring `extractText(File file)` and `supports(String extension)`.
- Multiple concrete implementations implement this interface: `PDFResumeParser`, `DOCXResumeParser`, and `TextResumeParser`.
- In `ResumeService`, we iterate over a polymorphic collection of `List<ResumeParser>` to automatically resolve and invoke the correct extraction algorithm based on the uploaded file type at runtime.

### Q4. Which Java Collection classes did you use and why?
**Answer**:
- **`HashSet` & `LinkedHashSet`**: In `SkillExtractor`, we store canonical skills in a `LinkedHashSet` to guarantee $O(1)$ lookup time and eliminate duplicate skill extractions.
- **`HashMap`**: In `SkillExtractor`, we map alias variations (e.g., `"core java" -> "Java"`, `"js" -> "JavaScript"`) to their canonical forms.
- **`ArrayList`**: In DAOs and services, we use `ArrayList` to hold ordered, dynamically-sized collections of candidate skills, experience records, and job listings.
- **`ConcurrentHashMap`**: In `SessionManager`, we manage active user authentication tokens thread-safely across concurrent HTTP requests.

### Q5. What is `try-with-resources` and where is it used?
**Answer**:
`try-with-resources` is a Java construct introduced in Java 7 that automatically closes resources implementing the `AutoCloseable` interface (such as `Connection`, `PreparedStatement`, `ResultSet`, `FileInputStream`, and `PDDocument`). We use it across all our DAOs and file parsers to prevent database connection leaks and file handle locks.

---

## Section 2: JDBC & Database Architecture

### Q6. Why did you use `PreparedStatement` instead of regular `Statement`?
**Answer**:
Two fundamental reasons:
1. **Security**: `PreparedStatement` precompiles the SQL template and treats all user inputs strictly as parameter values, preventing **SQL Injection (SQLi)** attacks.
2. **Performance**: MySQL precompiles and caches the execution plan, allowing faster repeated executions with different parameter sets.

### Q7. How does BlindHire prevent data leakage between candidate identity and screening scores?
**Answer**:
Through **Identity Quarantine Architecture**:
- Personal identity data (name, email, phone, address) is stored exclusively in `candidate_identity`.
- Qualification data is stored in `candidates`, `candidate_skills`, `candidate_education`, and `screening_results`.
- The regular candidate endpoints (`/api/candidates` and `/api/candidates/{id}`) never query or join `candidate_identity`.
- Identity can only be retrieved through `POST /api/candidates/{id}/reveal`, which requires authentication, authorization, and mandatory justification logging.

### Q8. What happens if a job or candidate is deleted? How are orphans avoided?
**Answer**:
All foreign keys in our MySQL schema are configured with `ON DELETE CASCADE`. If a candidate or job is removed, MySQL automatically deletes all associated skills, education records, experiences, projects, screening scores, and resumes, preserving relational integrity.

---

## Section 3: Web Server & Network Architecture

### Q9. How does Java's built-in `HttpServer` handle concurrent requests?
**Answer**:
In `HttpServerManager.java`, we configure an `ExecutorService` backed by a fixed thread pool:
```java
server.setExecutor(Executors.newFixedThreadPool(12));
```
When an HTTP request arrives, the server dispatches the request to an available worker thread from the pool. This allows multiple recruiters to browse, upload resumes, and view candidates simultaneously without blocking.

### Q10. What is CORS and how does BlindHire handle it?
**Answer**:
Cross-Origin Resource Sharing (CORS) is a browser security mechanism that restricts HTTP requests made by frontend scripts to a different origin (domain, protocol, or port). We implemented `CORSHandler.java` which sets standard response headers (`Access-Control-Allow-Origin: *`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Headers`) and intercepts HTTP `OPTIONS` preflight requests, returning HTTP 204 No Content.

---

## Section 4: Security & Cryptography

### Q11. How are recruiter passwords protected in BlindHire?
**Answer**:
We use Java's standard cryptographic provider `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")`.
- **PBKDF2** (Password-Based Key Derivation Function 2) applies **65,536 iterations** of HMAC-SHA256.
- A unique, cryptographically random **16-byte salt** is generated for every user using `SecureRandom`.
- This ensures that two users with identical passwords have completely different hashes, protecting against **Rainbow Table** and precomputed dictionary attacks.

### Q12. What is a timing attack and how do you prevent it?
**Answer**:
A timing attack is a side-channel vulnerability where an attacker measures the precise time taken by a password comparison function. In standard `String.equals()`, execution terminates at the first mismatched character, leaking information about how many characters were correct. In `PasswordUtil.java`, we implemented `slowEquals()` using bitwise XOR (`diff |= a[i] ^ b[i]`), ensuring that comparisons always execute in constant time regardless of where mismatches occur.

---

## Section 5: Resume Parsing & Screening Algorithm

### Q13. How does BlindHire extract text from PDF and DOCX files?
**Answer**:
- **PDF**: We use **Apache PDFBox** (`PDDocument.load(file)` and `PDFTextStripper`), extracting ordered positional text streams.
- **DOCX**: We use **Apache POI** (`XWPFDocument` and `XWPFWordExtractor`). As a resilient fallback, we also built a pure Core Java `ZipInputStream` reader that parses `word/document.xml` directly, guaranteeing zero downtime even if POI dependencies are missing.

### Q14. What formula does BlindHire use to calculate match scores?
**Answer**:
$$\text{Final Score} = (\text{Skill Score} \times 0.50) + (\text{Experience Score} \times 0.25) + (\text{Education Score} \times 0.15) + (\text{Project Score} \times 0.10)$$
- **Skills (50%)**: Compares required skills (75% weight) and preferred skills (25% weight).
- **Experience (25%)**: Compares candidate total years against minimum job requirements.
- **Education (15%)**: Evaluates degree hierarchy (M.Tech = 100%, B.Tech/MCA = 95%, BCA = 80%).
- **Projects (10%)**: Measures hands-on technical project portfolio evidence.

### Q15. Is BlindHire an automated decision maker?
**Answer**:
No. BlindHire is strictly designed as a **decision-support screening aid**. It provides transparent, explainable scores and lists matched and missing skills to help human recruiters prioritize review queues fairly, without replacing human judgment or making autonomous hiring decisions.
