package com.blindhire.model;

/**
 * Model representing an extracted technical or domain skill for a candidate.
 */
public class CandidateSkill {
    private int id;
    private int candidateId;
    private String skillName;

    public CandidateSkill() {}

    public CandidateSkill(int id, int candidateId, String skillName) {
        this.id = id;
        this.candidateId = candidateId;
        this.skillName = skillName;
    }

    public CandidateSkill(String skillName) {
        this.skillName = skillName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
}
