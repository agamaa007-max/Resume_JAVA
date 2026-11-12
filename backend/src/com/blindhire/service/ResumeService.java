package com.blindhire.service;

import com.blindhire.dao.*;
import com.blindhire.model.*;
import com.blindhire.parser.*;
import java.io.File;
import java.util.*;

/**
 * ResumeService orchestrates resume validation, text extraction,
 * qualification extraction, anonymization, persistence, and automated screening.
 */
public class ResumeService {

    private final CandidateDAO candidateDAO;
    private final JobDAO jobDAO;
    private final ResumeDAO resumeDAO;
    private final ScreeningDAO screeningDAO;
    private final AuditDAO auditDAO;
    private final AnonymizationService anonymizationService;
    private final MatchingService matchingService;

    private final List<ResumeParser> parsers;

    public ResumeService() {
        this.candidateDAO = new CandidateDAO();
        this.jobDAO = new JobDAO();
        this.resumeDAO = new ResumeDAO();
        this.screeningDAO = new ScreeningDAO();
        this.auditDAO = new AuditDAO();
        this.anonymizationService = new AnonymizationService();
        this.matchingService = new MatchingService();

        this.parsers = Arrays.asList(
            new PDFResumeParser(),
            new DOCXResumeParser(),
            new TextResumeParser()
        );
    }

    public Map<String, Object> processUploadedResume(File file, String originalFilename, int jobId, int recruiterId) throws Exception {
        if (file == null || !file.exists() || file.length() == 0) {
            throw new IllegalArgumentException("Uploaded resume file is empty or missing.");
        }

        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Selected job (ID: " + jobId + ") does not exist.");
        }

        // 1. Determine file extension
        String extension = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalFilename.substring(dot + 1).toLowerCase();
        }

        // 2. Select appropriate parser
        ResumeParser selectedParser = null;
        for (ResumeParser p : parsers) {
            if (p.supports(extension)) {
                selectedParser = p;
                break;
            }
        }

        if (selectedParser == null) {
            throw new IllegalArgumentException("Unsupported document type: '." + extension + "'. Supported formats: PDF, DOCX, TXT.");
        }

        // 3. Extract text
        String extractedText;
        try {
            extractedText = selectedParser.extractText(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from resume: " + e.getMessage(), e);
        }

        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new IllegalArgumentException("The uploaded resume contains no extractable text. Scanned images or OCR-only PDFs are not supported.");
        }

        // 4. Extract entities using SkillExtractor
        List<String> extractedSkills = SkillExtractor.extractSkills(extractedText);
        CandidateIdentity identity = SkillExtractor.extractIdentity(extractedText);
        List<Education> educations = SkillExtractor.extractEducation(extractedText);
        List<Experience> experiences = SkillExtractor.extractExperience(extractedText);
        List<Project> projects = SkillExtractor.extractProjects(extractedText);

        // 5. Generate Anonymous Candidate Profile
        Candidate candidate = anonymizationService.createAnonymousCandidate();
        candidate.setStatus("SCREENED");
        candidate = candidateDAO.saveCandidate(candidate);

        int candidateId = candidate.getId();

        // 6. Save Isolated Identity Information
        identity.setCandidateId(candidateId);
        candidateDAO.saveCandidateIdentity(identity);

        // 7. Save Skills
        if (!extractedSkills.isEmpty()) {
            candidateDAO.saveCandidateSkills(candidateId, extractedSkills);
        }
        List<CandidateSkill> candSkills = new ArrayList<>();
        for (String s : extractedSkills) candSkills.add(new CandidateSkill(s));
        candidate.setSkills(candSkills);

        // 8. Save Education
        for (Education edu : educations) {
            edu.setCandidateId(candidateId);
            candidateDAO.saveEducation(edu);
        }
        candidate.setEducation(educations);

        // 9. Save Experience
        for (Experience exp : experiences) {
            exp.setCandidateId(candidateId);
            candidateDAO.saveExperience(exp);
        }
        candidate.setExperience(experiences);

        // 10. Save Projects
        for (Project proj : projects) {
            proj.setCandidateId(candidateId);
            candidateDAO.saveProject(proj);
        }
        candidate.setProjects(projects);

        // 11. Save Resume File Record
        resumeDAO.saveResume(candidateId, originalFilename, file.getAbsolutePath(), extension.toUpperCase(), extractedText);

        // 12. Execute Matching Algorithm
        ScreeningResult screening = matchingService.calculateScreening(candidate, job);
        screeningDAO.saveScreeningResult(screening);

        // 13. Audit Log Entry
        auditDAO.logAction(recruiterId, "RESUME_SCREENED", candidateId,
                String.format("Uploaded & screened resume for %s against job '%s'. Score: %.1f%%",
                        candidate.getCandidateCode(), job.getTitle(), screening.getFinalScore()));

        // 14. Return explainable result payload
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("candidateId", candidateId);
        responseData.put("candidateCode", candidate.getCandidateCode());
        responseData.put("jobId", job.getId());
        responseData.put("jobTitle", job.getTitle());
        responseData.put("finalScore", screening.getFinalScore());
        responseData.put("skillScore", screening.getSkillScore());
        responseData.put("experienceScore", screening.getExperienceScore());
        responseData.put("educationScore", screening.getEducationScore());
        responseData.put("projectScore", screening.getProjectScore());
        responseData.put("matchedSkills", screening.getMatchedSkills());
        responseData.put("missingSkills", screening.getMissingSkills());
        responseData.put("explanation", screening.getScoreExplanation());
        responseData.put("extractedSkills", extractedSkills);

        return responseData;
    }
}
