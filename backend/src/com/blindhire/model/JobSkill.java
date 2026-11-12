package com.blindhire.model;

/**
 * Model representing a Skill attached to a Job posting.
 */
public class JobSkill {
    private int id;
    private int jobId;
    private String skillName;
    private String skillType; // "REQUIRED" or "PREFERRED"

    public JobSkill() {}

    public JobSkill(int id, int jobId, String skillName, String skillType) {
        this.id = id;
        this.jobId = jobId;
        this.skillName = skillName;
        this.skillType = skillType;
    }

    public JobSkill(String skillName, String skillType) {
        this.skillName = skillName;
        this.skillType = skillType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getSkillType() { return skillType; }
    public void setSkillType(String skillType) { this.skillType = skillType; }
}
