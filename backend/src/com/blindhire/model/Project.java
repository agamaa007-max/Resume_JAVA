package com.blindhire.model;

/**
 * Model representing an academic or professional project undertaken by a candidate.
 */
public class Project {
    private int id;
    private int candidateId;
    private String projectName;
    private String description;
    private String technologies;

    public Project() {}

    public Project(int id, int candidateId, String projectName, String description, String technologies) {
        this.id = id;
        this.candidateId = candidateId;
        this.projectName = projectName;
        this.description = description;
        this.technologies = technologies;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTechnologies() { return technologies; }
    public void setTechnologies(String technologies) { this.technologies = technologies; }
}
