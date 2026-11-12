package com.blindhire.model;

/**
 * Model representing an educational qualification for a candidate.
 */
public class Education {
    private int id;
    private int candidateId;
    private String degree;
    private String institution;
    private String field;
    private int graduationYear;
    private double percentage;

    public Education() {}

    public Education(int id, int candidateId, String degree, String institution, String field, int graduationYear, double percentage) {
        this.id = id;
        this.candidateId = candidateId;
        this.degree = degree;
        this.institution = institution;
        this.field = field;
        this.graduationYear = graduationYear;
        this.percentage = percentage;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public int getGraduationYear() { return graduationYear; }
    public void setGraduationYear(int graduationYear) { this.graduationYear = graduationYear; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
}
