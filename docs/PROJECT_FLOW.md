# BlindHire - System Workflows & Diagrams

This document details the end-to-end data flow, system architecture diagrams, and sequence diagrams for the BlindHire application.

---

## 1. High-Level Recruitment Workflow

```mermaid
flowchart TD
    A["Recruiter Registers / Logs In"] --> B["Recruiter Creates Job Posting"]
    B --> C["Recruiter Defines Required & Preferred Skills"]
    C --> D["Recruiter Uploads Resume (PDF / DOCX)"]
    D --> E["Apache PDFBox / POI Extracts Text"]
    E --> F["SkillExtractor Extracts Skills, Education, Experience"]
    F --> G["AnonymizationService Generates Code (e.g. CAND-1001)"]
    G --> H["PII Quarantined in candidate_identity Table"]
    H --> I["MatchingService Calculates Explainable Multi-Factor Score"]
    I --> J["Recruiter Evaluates Anonymous Qualifications"]
    J --> K{"Score Meets Threshold?"}
    K -- "No" --> L["Candidate Flagged or Rejected Anonymously"]
    K -- "Yes" --> M["Recruiter Requests Identity Reveal"]
    M --> N["Recruiter Enters Mandatory Justification Reason"]
    N --> O["Audit Log Recorded in MySQL"]
    O --> P["Identity Information Displayed to Recruiter"]
```

---

## 2. Sequence Diagram: Resume Ingestion & Screening Engine

```mermaid
sequenceDiagram
    autonumber
    actor Recruiter as Recruiter (Browser)
    participant Server as HttpServer / Router
    participant RHandler as ResumeHandler
    participant RService as ResumeService
    participant Parser as PDFResumeParser / DOCXResumeParser
    participant Extractor as SkillExtractor
    participant Anon as AnonymizationService
    participant Matcher as MatchingService
    participant DAO as CandidateDAO / ScreeningDAO
    participant DB as MySQL Database

    Recruiter->>Server: POST /api/resumes/upload (File + Job ID)
    Server->>RHandler: handleResumeUpload(exchange)
    RHandler->>RService: processUploadedResume(file, jobId, recruiterId)
    RService->>Parser: extractText(file)
    Parser-->>RService: Raw document text
    RService->>Extractor: extractSkills(text), extractIdentity(text), etc.
    Extractor-->>RService: Extracted entities
    RService->>Anon: createAnonymousCandidate()
    Anon-->>RService: Candidate (e.g. CAND-1001)
    RService->>DAO: saveCandidate(), saveIdentity(), saveSkills(), saveEducation()
    DAO->>DB: INSERT INTO candidates, candidate_identity, candidate_skills...
    RService->>Matcher: calculateScreening(candidate, job)
    Matcher-->>RService: ScreeningResult (finalScore, breakdown, explanation)
    RService->>DAO: saveScreeningResult(result)
    DAO->>DB: INSERT INTO screening_results
    RService-->>RHandler: Screening response payload
    RHandler-->>Recruiter: JSON 201 Created (Score + Matched/Missing Skills)
```

---

## 3. Sequence Diagram: Controlled Identity Reveal Flow

```mermaid
sequenceDiagram
    autonumber
    actor Recruiter as Recruiter (Browser)
    participant Server as HttpServer / Router
    participant CHandler as CandidateHandler
    participant IService as IdentityService
    participant ADAO as AuditDAO
    participant CDAO as CandidateDAO
    participant DB as MySQL Database

    Recruiter->>Server: POST /api/candidates/1/reveal (reason: "Shortlisted for Round 1")
    Server->>CHandler: handleRevealIdentity()
    CHandler->>IService: revealIdentity(candidateId, recruiterId, reason)
    Note over IService: Verifies reason length >= 5 chars
    IService->>ADAO: logAction(recruiterId, 'IDENTITY_REVEAL', candidateId, reason)
    ADAO->>DB: INSERT INTO audit_logs
    IService->>CDAO: getCandidateIdentity(candidateId)
    CDAO->>DB: SELECT * FROM candidate_identity WHERE candidate_id = 1
    DB-->>CDAO: Identity record (Name, Email, Phone, Address)
    CDAO-->>IService: CandidateIdentity
    IService-->>CHandler: CandidateIdentity
    CHandler-->>Recruiter: JSON 200 OK (Revealed Identity)
```

---

## 4. Multi-Factor Scoring Mathematical Model

BlindHire calculates the overall match score using a four-factor weighted formula:

$$\text{Final Score} = (\text{Skill Score} \times 0.50) + (\text{Experience Score} \times 0.25) + (\text{Education Score} \times 0.15) + (\text{Project Score} \times 0.10)$$

### 1. Skill Score (50% Overall Weight)
Compares candidate skills against job skill taxonomy:
$$\text{Skill Score} = \left(\frac{\text{Matched Required}}{\text{Total Required}} \times 80\right) + \left(\frac{\text{Matched Preferred}}{\text{Total Preferred}} \times 20\right)$$

### 2. Experience Score (25% Overall Weight)
Compares candidate verifiable experience years ($E_c$) with job minimum requirement ($E_{\min}$):
$$\text{Experience Score} = \begin{cases} 
100\% & \text{if } E_c \ge E_{\min} \text{ or } E_{\min} = 0 \\ 
\left(\frac{E_c}{E_{\min}}\right) \times 100\% & \text{if } E_c < E_{\min} 
\end{cases}$$

### 3. Education Score (15% Overall Weight)
Evaluates educational benchmarks based on degree hierarchy (M.Tech/PhD = 100%, B.Tech/MCA = 95%, BCA/B.Sc = 80%, Diploma = 70%), adjusted by academic grade percentages.

### 4. Project Score (10% Overall Weight)
Quantifies practical engineering demonstrations:
- 3+ Relevant Projects: 95%
- 2 Projects: 85%
- 1 Project: 70%
- 0 Projects: 50%
