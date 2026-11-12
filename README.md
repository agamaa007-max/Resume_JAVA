# BlindHire
### Blind Resume Screening & Applicant Tracking System
*Built with HTML5, CSS3, Vanilla JavaScript, Core Java (JDK 17+), JDBC, and MySQL.*

---

## 1. Project Introduction

**BlindHire** is an academic software development project designed to combat unconscious bias in technical recruitment. Conventional hiring workflows expose evaluators to personal demographic information (candidate name, gender, contact details, physical address, and photos) before professional qualifications are assessed. 

BlindHire implements an **Identity-Quarantined Recruitment Pipeline** where:
- Personal identity details (PII) are quarantined in an isolated MySQL database table.
- Candidates are represented exclusively by anonymous synthetic IDs (e.g., `CAND-1001`).
- Resume text is automatically extracted from **PDF** and **DOCX** files using **Apache PDFBox** and **Apache POI**.
- Candidate qualifications are evaluated using a transparent, explainable multi-factor scoring model.
- Contact identity can only be revealed through an authorized workflow with mandatory justification logging in an immutable audit trail.

---

## 2. Technology Stack

| Component | Technology | Rationale |
|---|---|---|
| **Frontend Structure** | HTML5 | Semantic, standards-compliant web markup |
| **Frontend Styling** | CSS3 | Custom CSS variables, responsive SaaS layout, no CSS bloat |
| **Frontend Logic** | Vanilla JavaScript (ES6+) | Standard Fetch API, centralized API client, no React/Node |
| **Backend Language** | Core Java (JDK 17+) | Pure object-oriented programming without Spring Boot |
| **HTTP Server** | Built-in `HttpServer` | High-throughput built-in `com.sun.net.httpserver` with thread pool |
| **Persistence** | JDBC (`PreparedStatement`) | Deterministic resource management, SQL injection defense |
| **Database** | MySQL 8.0 | Relational database enforcing constraints & foreign key cascades |
| **JSON Serialization** | Google Gson (2.10.1) | Fast, standards-compliant JSON serialization |
| **PDF Extraction** | Apache PDFBox (2.0.30) | Industry-standard PDF text stripper |
| **DOCX Extraction** | Apache POI (5.2.5) | Word document extractor with resilient XML fallback |
| **Password Security** | PBKDF2WithHmacSHA256 | 65,536 iterations, 16-byte random salt per user |

---

## 3. Project Folder Structure

```text
BlindHire/
│
├── frontend/                     # HTML, CSS, and Vanilla JavaScript Client
│   ├── index.html                # Product landing & overview page
│   ├── login.html                # Recruiter sign-in portal
│   ├── register.html             # Recruiter registration portal
│   ├── dashboard.html            # Main recruitment operations dashboard
│   ├── jobs.html                 # Job vacancies & skills taxonomy
│   ├── create-job.html           # Post new job vacancy form
│   ├── upload-resume.html        # PDF & DOCX upload and parser
│   ├── candidates.html           # Blind candidate screening & filtering table
│   ├── candidate-details.html    # In-depth qualification & identity reveal view
│   ├── audit-logs.html           # Immutable compliance audit trail
│   ├── profile.html              # Recruiter profile & password manager
│   ├── css/                      # Modular CSS stylesheets
│   │   ├── components.css        # Buttons, cards, badges, tables, modals
│   │   ├── main.css              # Application layout & sidebar navigation
│   │   ├── auth.css              # Authentication form styles
│   │   ├── dashboard.css         # Metrics & statistics layout
│   │   ├── jobs.css              # Job cards & skill tags
│   │   └── candidates.css        # Candidate filters & score gauges
│   └── js/                       # Vanilla JavaScript client logic
│       ├── api.js                # Centralized Fetch client & session tokens
│       ├── auth.js               # Login & registration handlers
│       ├── dashboard.js          # Live dashboard metrics loader
│       ├── jobs.js               # Job vacancy controller
│       ├── upload.js             # File upload & extraction orchestrator
│       ├── candidates.js         # Candidate search & filter engine
│       ├── candidate-details.js  # Score breakdown & authorized reveal
│       └── audit-logs.js         # Audit log renderer
│
├── backend/                      # Core Java Backend Source Code
│   ├── bin/                      # Compiled Java class binaries (.class)
│   ├── lib/                      # External JAR libraries (Managed dependencies)
│   │   ├── mysql-connector-j-8.3.0.jar
│   │   ├── gson-2.10.1.jar
│   │   ├── pdfbox-2.0.30.jar
│   │   ├── fontbox-2.0.30.jar
│   │   ├── commons-logging-1.2.jar
│   │   ├── poi-5.2.5.jar
│   │   └── README.md
│   └── src/com/blindhire/        # Layered Java Packages
│       ├── Main.java             # Server entry point & boot orchestrator
│       ├── config/               # AppConfig (properties & environment loader)
│       ├── database/             # DBConnection & DatabaseInitializer
│       ├── model/                # User, Job, Candidate, ScreeningResult, etc.
│       ├── dao/                  # UserDAO, JobDAO, CandidateDAO, ResumeDAO...
│       ├── service/              # AuthService, JobService, MatchingService...
│       ├── parser/               # PDFResumeParser, DOCXResumeParser, SkillExtractor
│       ├── security/             # PasswordUtil (PBKDF2), SessionManager, AuthMiddleware
│       ├── util/                 # JSONUtil, ResponseUtil, FileUtil, CodeGenerator
│       ├── handler/              # AuthHandler, JobHandler, ResumeHandler...
│       └── server/               # HttpServerManager, RequestRouter, CORSHandler
│
├── database/                     # MySQL Relational Scripts
│   ├── schema.sql                # Complete DDL tables, keys, and indexes
│   ├── sample-data.sql           # Synthetic recruiters, jobs, and candidates
│   └── reset-database.sql        # Database drop and rebuild script
│
├── scripts/                      # Windows Automation Batch Scripts
│   ├── compile.bat               # Compiles Core Java sources with JARs
│   ├── run.bat                   # Starts the HTTP server on port 8080
│   ├── compile-and-run.bat       # One-click build and execution
│   ├── setup-database.bat        # MySQL schema and data initializer
│   ├── download-libs.bat         # Automates downloading external JARs
│   └── download-libs.ps1         # PowerShell dependency downloader
│
├── uploads/resumes/              # Safe file storage for uploaded resumes
├── docs/                         # Academic Presentation & Technical Documentation
│   ├── PROJECT_OVERVIEW.md       # Problem statement, philosophy, architecture
│   ├── DATABASE_DESIGN.md        # Detailed ER schema, table specs, keys
│   ├── API_DOCUMENTATION.md      # Full REST endpoint schemas & examples
│   ├── PROJECT_FLOW.md           # Sequence diagrams & mathematical model
│   └── VIVA_QUESTIONS.md         # 35+ Examination questions and detailed answers
│
├── config.properties             # Local configuration file (DB credentials)
├── .gitignore                    # Git ignore file
└── README.md                     # Master project documentation
```

---

## 4. Multi-Factor Scoring Formula

The matching engine calculates an objective qualification score using weighted factors:

$$\text{Final Score} = (\text{Skill Score} \times 0.50) + (\text{Experience Score} \times 0.25) + (\text{Education Score} \times 0.15) + (\text{Project Score} \times 0.10)$$

- **Skills Match (50%)**: Evaluates required skills (75% of skill portion) and preferred skills (25% of skill portion).
- **Experience Match (25%)**: Compares verifiable years against minimum job requirements.
- **Education Match (15%)**: Assesses degree hierarchy (M.Tech/PhD = 100%, B.Tech/MCA = 95%, BCA/B.Sc = 80%).
- **Project Demonstrations (10%)**: Analyzes candidate hands-on technical project portfolio.

---

## 5. Quick Start Instructions

### Prerequisites
1. **JDK 17 or newer**: Ensure `java -version` and `javac -version` work in your terminal.
2. **MySQL Server 8.0**: Running locally on `localhost:3306`.

### Step 1: Configure Database Credentials
Edit `config.properties` in the project root if your MySQL password is not empty:
```properties
db.host=localhost
db.port=3306
db.name=blindhire
db.user=root
db.password=YourMySQLPasswordHere
server.port=8080
```

### Step 2: One-Click Compile & Run
Open terminal in the project directory and run:
```bat
scripts\compile-and-run.bat
```

### Step 3: Access the Application
Open your web browser and navigate to:
```text
http://localhost:8080
```

---

## 6. Default Demo Credentials

BlindHire is pre-loaded with synthetic recruiters and candidates for college demonstrations:

| Account | Email | Password | Role |
|---|---|---|---|
| **Lead Recruiter** | `recruiter@blindhire.com` | `Password@123` | Full Recruitment Admin |
| **HR Specialist** | `sarah.hr@blindhire.com` | `Password@123` | Talent Evaluator |
| **Technical Recruiter** | `david.talent@blindhire.com` | `Password@123` | Technical Screener |

---

## 7. College Project Presentation Walkthrough

Follow this sequence during your project demonstration:

```text
1. Open http://localhost:8080 -> Review the BlindHire landing page.
2. Sign in with demo credentials (recruiter@blindhire.com / Password@123).
3. View the Operations Dashboard -> Note the live MySQL counters.
4. Navigate to Job Vacancies -> Show the "Java Backend Developer" opening.
5. Create a new Job Posting -> Define required and preferred skills.
6. Upload a Resume (PDF or DOCX) -> Demonstrate Apache PDFBox/POI text extraction.
7. Observe anonymous ID generation (CAND-1006) and live score calculation.
8. Filter candidates in Candidates table -> Search by score and skills.
9. Open Candidate Details -> Demonstrate blind qualification evaluation.
10. Click "Reveal Identity" -> Enter mandatory justification reason.
11. Observe identity reveal and verify the corresponding entry in Audit Logs!
```

---

## 8. Academic Project Deliverables & References

- [Project Overview](file:///docs/PROJECT_OVERVIEW.md)
- [Database Design](file:///docs/DATABASE_DESIGN.md)
- [API Documentation](file:///docs/API_DOCUMENTATION.md)
- [System Architecture & Sequence Flows](file:///docs/PROJECT_FLOW.md)
- [College Viva Examination Questions & Answers](file:///docs/VIVA_QUESTIONS.md)
