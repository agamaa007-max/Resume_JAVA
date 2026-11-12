package com.blindhire.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing the explainable multi-factor screening evaluation for a candidate against a job.
 */
public class ScreeningResult {
    private int id;
    private int candidateId;
    private int jobId;
    private double skillScore;
    private double experienceScore;
    private double educationScore;
    private double projectScore;
    private double finalScore;
    private Timestamp screenedAt;

    // View-helper attributes (populated during service queries)
    private String candidateCode;
    private String jobTitle;
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private String scoreExplanation;

    public ScreeningResult() {}

    public ScreeningResult(int id, int candidateId, int jobId, double skillScore,
                           double experienceScore, double educationScore, double projectScore,
                           double finalScore, Timestamp screenedAt) {
        this.id = id;
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.skillScore = skillScore;
        this.experienceScore = experienceScore;
        this.educationScore = educationScore;
        this.projectScore = projectScore;
        this.finalScore = finalScore;
        this.screenedAt = screenedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public double getSkillScore() { return skillScore; }
    public void setSkillScore(double skillScore) { this.skillScore = skillScore; }

    public double getExperienceScore() { return experienceScore; }
    public void setExperienceScore(double experienceScore) { this.experienceScore = experienceScore; }

    public double getEducationScore() { return educationScore; }
    public void setEducationScore(double educationScore) { this.educationScore = educationScore; }

    public double getProjectScore() { return projectScore; }
    public void setProjectScore(double projectScore) { this.projectScore = projectScore; }

    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }

    public Timestamp getScreenedAt() { return screenedAt; }
    public void setScreenedAt(Timestamp screenedAt) { this.screenedAt = screenedAt; }

    public String getCandidateCode() { return candidateCode; }
    public void setCandidateCode(String candidateCode) { this.candidateCode = candidateCode; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public String getScoreExplanation() { return scoreExplanation; }
    public void setScoreExplanation(String scoreExplanation) { this.scoreExplanation = scoreExplanation; }
}
