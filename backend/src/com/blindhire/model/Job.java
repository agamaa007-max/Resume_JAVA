package com.blindhire.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a Job posting created by a Recruiter.
 */
public class Job {
    private int id;
    private String title;
    private String description;
    private String minimumEducation;
    private int minimumExperience;
    private int createdBy;
    private Timestamp createdAt;
    private String status; // "ACTIVE", "CLOSED"
    private List<JobSkill> skills = new ArrayList<>();

    public Job() {}

    public Job(int id, String title, String description, String minimumEducation,
               int minimumExperience, int createdBy, Timestamp createdAt, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.minimumEducation = minimumEducation;
        this.minimumExperience = minimumExperience;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMinimumEducation() { return minimumEducation; }
    public void setMinimumEducation(String minimumEducation) { this.minimumEducation = minimumEducation; }

    public int getMinimumExperience() { return minimumExperience; }
    public void setMinimumExperience(int minimumExperience) { this.minimumExperience = minimumExperience; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<JobSkill> getSkills() { return skills; }
    public void setSkills(List<JobSkill> skills) { this.skills = skills; }

    public void addSkill(JobSkill skill) {
        if (this.skills == null) {
            this.skills = new ArrayList<>();
        }
        this.skills.add(skill);
    }
}
