package com.blindhire.model;

/**
 * Model representing professional work experience for a candidate.
 */
public class Experience {
    private int id;
    private int candidateId;
    private String company;
    private String jobTitle;
    private double yearsOfExperience;
    private String description;

    public Experience() {}

    public Experience(int id, int candidateId, String company, String jobTitle, double yearsOfExperience, String description) {
        this.id = id;
        this.candidateId = candidateId;
        this.company = company;
        this.jobTitle = jobTitle;
        this.yearsOfExperience = yearsOfExperience;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public double getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(double yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
