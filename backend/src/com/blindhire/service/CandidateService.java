package com.blindhire.service;

import com.blindhire.dao.CandidateDAO;
import com.blindhire.dao.JobDAO;
import com.blindhire.dao.ScreeningDAO;
import com.blindhire.model.Candidate;
import com.blindhire.model.CandidateSkill;
import com.blindhire.model.Job;
import com.blindhire.model.ScreeningResult;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CandidateService coordinates anonymous candidate retrieval, search, and filtering.
 */
public class CandidateService {

    private final CandidateDAO candidateDAO;
    private final ScreeningDAO screeningDAO;
    private final JobDAO jobDAO;

    public CandidateService() {
        this.candidateDAO = new CandidateDAO();
        this.screeningDAO = new ScreeningDAO();
        this.jobDAO = new JobDAO();
    }

    public CandidateService(CandidateDAO candidateDAO, ScreeningDAO screeningDAO, JobDAO jobDAO) {
        this.candidateDAO = candidateDAO;
        this.screeningDAO = screeningDAO;
        this.jobDAO = jobDAO;
    }

    public List<Candidate> getAllCandidates() throws SQLException {
        return candidateDAO.getAllCandidates();
    }

    public Candidate getCandidateById(int id) throws SQLException {
        return candidateDAO.getCandidateById(id);
    }

    public Candidate getCandidateByCode(String code) throws SQLException {
        return candidateDAO.getCandidateByCode(code);
    }

    /**
     * Retrieves candidate summary rows enriched with screening scores for display in the candidates table.
     */
    public List<Map<String, Object>> getScreenedCandidateSummaries(Integer jobId, Double minScore, String skillFilter, String search) throws SQLException {
        List<Candidate> allCandidates = candidateDAO.getAllCandidates();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Candidate c : allCandidates) {
            // Apply search filter (candidate code)
            if (search != null && !search.trim().isEmpty()) {
                if (!c.getCandidateCode().toLowerCase().contains(search.toLowerCase().trim())) {
                    continue;
                }
            }

            // Apply skill filter
            if (skillFilter != null && !skillFilter.trim().isEmpty()) {
                boolean hasSkill = false;
                for (CandidateSkill cs : c.getSkills()) {
                    if (cs.getSkillName().equalsIgnoreCase(skillFilter.trim())) {
                        hasSkill = true;
                        break;
                    }
                }
                if (!hasSkill) continue;
            }

            // Retrieve screening score for selected job or best score
            double score = 0.0;
            String jobTitle = "Not Screened";
            if (jobId != null && jobId > 0) {
                ScreeningResult sr = screeningDAO.getScreeningResultByCandidate(c.getId(), jobId);
                if (sr != null) {
                    score = sr.getFinalScore();
                    jobTitle = sr.getJobTitle();
                } else {
                    continue; // Skip candidates not screened for this specific job filter
                }
            } else {
                // Find latest screening result
                List<ScreeningResult> allSr = screeningDAO.getAllScreeningResults();
                for (ScreeningResult sr : allSr) {
                    if (sr.getCandidateId() == c.getId()) {
                        score = sr.getFinalScore();
                        jobTitle = sr.getJobTitle();
                        break;
                    }
                }
            }

            // Apply min score filter
            if (minScore != null && score < minScore) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("candidateCode", c.getCandidateCode());
            map.put("status", c.getStatus());
            map.put("score", score);
            map.put("jobTitle", jobTitle);

            // Extract skills list as comma-separated
            List<String> skillNames = new ArrayList<>();
            for (CandidateSkill cs : c.getSkills()) {
                skillNames.add(cs.getSkillName());
            }
            map.put("skills", skillNames);

            // Top Education
            String eduStr = "Not Specified";
            if (!c.getEducation().isEmpty()) {
                eduStr = c.getEducation().get(0).getDegree();
            }
            map.put("education", eduStr);

            // Total Experience
            double expYears = 0.0;
            for (var exp : c.getExperience()) {
                expYears += exp.getYearsOfExperience();
            }
            map.put("experience", expYears > 0 ? (expYears + " yrs") : "Fresher");

            results.add(map);
        }

        return results;
    }
}
