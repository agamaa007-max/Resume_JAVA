# BlindHire - Comprehensive Project Overview

## 1. Project Abstract

**BlindHire** is a privacy-conscious, bias-mitigating Applicant Tracking System (ATS) and resume screening engine developed as an academic software engineering project.

In conventional recruitment pipelines, evaluators are routinely exposed to demographic attributes—such as candidate full names, perceived gender, geographic locations, ethnic associations, and contact details—long before evaluating core technical qualifications. Multiple industrial studies demonstrate that this exposure introduces unconscious or conscious bias, leading to unfair evaluation disparities.

BlindHire solves this challenge by implementing an **Identity-Quarantined Recruitment Architecture**:
1. During initial resume ingestion, candidate personal identity attributes (PII) are isolated into a quarantined database table.
2. The screening engine assigns an anonymous synthetic code (e.g., `CAND-1001`).
3. Evaluators review qualifications, normalized skills, educational history, and project portfolios entirely in the blind.
4. Candidates are evaluated using an **explainable multi-factor scoring model**:
   - Technical Skills: 50%
   - Experience Duration: 25%
   - Educational Benchmark: 15%
   - Project Demonstrations: 10%
5. When a candidate meets the screening threshold, recruiters may perform a **controlled, audited identity reveal** requiring a mandatory documented justification that is permanently written to the system audit trail.

---

## 2. Problem Statement & Motivation

| Traditional Screening Flaws | BlindHire Mitigation |
|---|---|
| Demographic details create unconscious hiring bias | Candidate PII is quarantined; evaluators interact only with anonymous codes |
| Black-box automated rejection without explanation | Explainable, deterministic mathematical breakdown with explicit matched and missing skills |
| Complex enterprise frameworks obscure source code logic | Pure Core Java architecture using built-in standard library components and JDBC |
| Lack of accountability when viewing sensitive personal data | Tamper-evident audit logging for every identity reveal action |

---

## 3. Technology Stack & Architectural Decisions

BlindHire intentionally uses a clean, framework-free architecture that emphasizes foundational computer science principles:

- **Core Java (JDK 17+)**: Demonstrates object-oriented programming (OOP), encapsulation, inheritance, polymorphism, and standard library collections.
- **Java Built-in HTTP Server (`com.sun.net.httpserver.HttpServer`)**: Provides high-throughput, multi-threaded HTTP handling without bloated external servlet containers.
- **JDBC (`java.sql.*`)**: Uses `PreparedStatement` and `try-with-resources` to ensure deterministic resource disposal and complete defense against SQL injection attacks.
- **MySQL 8.0**: Relational persistence enforcing foreign key cascades, unique constraints, and optimized index structures.
- **HTML5, CSS3 & Vanilla JavaScript**: Clean, modern corporate SaaS interface using the standard browser Fetch API.
- **Apache PDFBox & Apache POI**: Industry-standard libraries for extracting raw textual content from PDF and DOCX resume documents.
- **Google Gson**: High-performance JSON serialization and deserialization.
- **PBKDF2WithHmacSHA256**: Cryptographically secure password hashing using 65,536 iterations and 16-byte unique salts.

---

## 4. Software Architecture Layers

The backend conforms strictly to a 5-tier separation of concerns:

```text
Frontend (HTML/CSS/JS)
         |  REST JSON / HTTP
         v
[Layer 5] Server & Request Router (HttpServerManager, RequestRouter, CORSHandler)
         |
         v
[Layer 4] Handlers (AuthHandler, JobHandler, ResumeHandler, CandidateHandler, ScreeningHandler)
         |
         v
[Layer 3] Services (AuthService, JobService, ResumeService, MatchingService, AnonymizationService)
         |
         v
[Layer 2] Data Access Objects - DAO (UserDAO, JobDAO, CandidateDAO, ResumeDAO, ScreeningDAO, AuditDAO)
         |  JDBC / PreparedStatement
         v
[Layer 1] Models & MySQL Database (User, Job, Candidate, ScreeningResult, Education, Experience...)
```

---

## 5. Ethical AI & Human-in-the-Loop Philosophy

BlindHire explicitly positions automated scoring as a **decision-support screening aid**, not an autonomous hiring authority. The system calculates a transparent percentage match to help human recruiters prioritize review queues while keeping human judgment central to final interview and employment decisions.
