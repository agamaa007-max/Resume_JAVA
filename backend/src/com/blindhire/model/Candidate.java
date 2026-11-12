package com.blindhire.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing an Anonymous Candidate profile in BlindHire.
 * Encapsulates qualifications while deliberately excluding personal identity attributes.
 */
public class Candidate {
    private int id;
    private String candidateCode; // e.g. "CAND-1001"
    private Timestamp createdAt;
    private String status; // "APPLIED", "SCREENED", "SHORTLISTED", "REJECTED"

    private List<CandidateSkill> skills = new ArrayList<>();
    private List<Education> education = new ArrayList<>();
    private List<Experience> experience = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();

    public Candidate() {}

    public Candidate(int id, String candidateCode, Timestamp createdAt, String status) {
        this.id = id;
        this.candidateCode = candidateCode;
        this.createdAt = createdAt;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCandidateCode() { return candidateCode; }
    public void setCandidateCode(String candidateCode) { this.candidateCode = candidateCode; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<CandidateSkill> getSkills() { return skills; }
    public void setSkills(List<CandidateSkill> skills) { this.skills = skills; }

    public List<Education> getEducation() { return education; }
    public void setEducation(List<Education> education) { this.education = education; }

    public List<Experience> getExperience() { return experience; }
    public void setExperience(List<Experience> experience) { this.experience = experience; }

    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }
}
