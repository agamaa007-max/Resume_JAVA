# BlindHire - REST API Documentation

BlindHire provides REST-style HTTP endpoints over Java's built-in `HttpServer`.
All API responses follow a uniform JSON structure:

```json
{
  "success": true,
  "message": "Human readable status message",
  "data": { ... }
}
```

---

## 1. Authentication Endpoints

### 1.1. Register Recruiter
- **URL**: `POST /api/auth/register`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "fullName": "Alex Morgan",
  "email": "alex@company.com",
  "password": "Password@123"
}
```
- **Response (201 Created)**:
```json
{
  "success": true,
  "message": "Registration successful.",
  "data": {
    "token": "dGhpcy1pcy1hLXNlY3VyZS10b2tlbg...",
    "userId": 4,
    "fullName": "Alex Morgan",
    "email": "alex@company.com"
  }
}
```

### 1.2. Recruiter Login
- **URL**: `POST /api/auth/login`
- **Request Body**:
```json
{
  "email": "recruiter@blindhire.com",
  "password": "Password@123"
}
```
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Login successful.",
  "data": {
    "token": "secure-session-token",
    "userId": 1,
    "fullName": "Alex Morgan",
    "email": "recruiter@blindhire.com"
  }
}
```

### 1.3. Sign Out
- **URL**: `POST /api/auth/logout`
- **Headers**: `Authorization: Bearer <token>`
- **Response (200 OK)**: `{ "success": true, "message": "Logged out successfully." }`

### 1.4. Current Recruiter Profile
- **URL**: `GET /api/auth/me`
- **Headers**: `Authorization: Bearer <token>`
- **Response (200 OK)**: Returns profile details with password hashes stripped.

---

## 2. Job Vacancy Endpoints

### 2.1. List All Jobs
- **URL**: `GET /api/jobs`
- **Response (200 OK)**: Array of job objects including required and preferred skills.

### 2.2. Create Job Posting
- **URL**: `POST /api/jobs`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Body**:
```json
{
  "title": "Java Backend Developer",
  "description": "Develop scalable microservices with Core Java and MySQL.",
  "minimumEducation": "B.Tech Computer Science",
  "minimumExperience": 2,
  "requiredSkills": ["Java", "SQL", "Spring Boot", "Git"],
  "preferredSkills": ["Docker", "AWS"],
  "status": "ACTIVE"
}
```
- **Response (201 Created)**: Created Job object with ID.

### 2.3. Get Job by ID
- **URL**: `GET /api/jobs/{id}`
- **Response (200 OK)**: Job details with attached skills.

### 2.4. Update Job
- **URL**: `PUT /api/jobs/{id}`
- **Headers**: `Authorization: Bearer <token>`
- **Response (200 OK)**: Updated job representation.

### 2.5. Delete Job
- **URL**: `DELETE /api/jobs/{id}`
- **Headers**: `Authorization: Bearer <token>`
- **Response (200 OK)**: `{ "success": true, "message": "Job deleted successfully." }`

---

## 3. Resume Ingestion & Screening Endpoints

### 3.1. Upload and Screen Resume
- **URL**: `POST /api/resumes/upload`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Body**:
```json
{
  "jobId": 1,
  "fileName": "JohnDoe_Resume.pdf",
  "fileBase64": "data:application/pdf;base64,JVBERi0xLjQKJ..."
}
```
- **Response (201 Created)**:
```json
{
  "success": true,
  "message": "Resume uploaded, extracted and screened successfully.",
  "data": {
    "candidateId": 6,
    "candidateCode": "CAND-1006",
    "jobId": 1,
    "jobTitle": "Java Backend Developer",
    "finalScore": 87.25,
    "skillScore": 90.0,
    "experienceScore": 85.0,
    "educationScore": 95.0,
    "projectScore": 80.0,
    "matchedSkills": ["Java", "SQL", "Git"],
    "missingSkills": ["Spring Boot (Required)"],
    "explanation": "Calculated Final Match: 87.25% ...",
    "extractedSkills": ["Java", "SQL", "Git", "Python"]
  }
}
```

---

## 4. Candidate Screening Endpoints

### 4.1. List Screened Candidates
- **URL**: `GET /api/candidates`
- **Query Parameters**:
  - `jobId` (optional): Filter candidates for a specific role
  - `minScore` (optional): Filter by minimum match percentage
  - `skill` (optional): Filter by required skill keyword
  - `search` (optional): Match candidate code (e.g. `CAND-1001`)
- **Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "candidateCode": "CAND-1001",
      "score": 95.75,
      "jobTitle": "Java Backend Developer",
      "skills": ["Java", "SQL", "Spring Boot", "Git"],
      "education": "B.Tech",
      "experience": "2 yrs"
    }
  ]
}
```

### 4.2. Candidate Details (Anonymous)
- **URL**: `GET /api/candidates/{id}`
- **Response (200 OK)**: Anonymous profile containing extracted skills, education, experience, and projects. PII is **not returned**.

---

## 5. Controlled Identity Reveal Endpoints

### 5.1. Reveal Candidate Contact Information
- **URL**: `POST /api/candidates/{id}/reveal`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Body**:
```json
{
  "reason": "Candidate attained 90%+ match score. Shortlisted for technical screening round."
}
```
- **Security Check**: Verifies recruiter authorization and writes audit entry to `audit_logs`.
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Identity successfully revealed and logged.",
  "data": {
    "candidateId": 1,
    "name": "Rahul Sharma",
    "email": "rahul.sharma@example.com",
    "phone": "+91 9876543210",
    "address": "New Delhi, India"
  }
}
```

---

## 6. Dashboard & Audit Endpoints

### 6.1. Live Dashboard Metrics
- **URL**: `GET /api/screening/dashboard`
- **Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "totalJobs": 3,
    "totalCandidates": 5,
    "totalResumes": 3,
    "totalScreened": 3,
    "averageScore": 79.2,
    "activeJobs": 3
  }
}
```

### 6.2. System Audit Trail
- **URL**: `GET /api/audit-logs`
- **Headers**: `Authorization: Bearer <token>`
- **Response (200 OK)**: List of tamper-evident audit records.
