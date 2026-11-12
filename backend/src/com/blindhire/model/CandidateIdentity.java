package com.blindhire.model;

/**
 * Model representing candidate personal identity.
 * CRITICAL PRIVACY ARCHITECTURE:
 * This model is kept strictly isolated from candidate screening evaluation.
 * Accessible only through authenticated identity reveal workflows with audit logging.
 */
public class CandidateIdentity {
    private int id;
    private int candidateId;
    private String name;
    private String email;
    private String phone;
    private String address;

    public CandidateIdentity() {}

    public CandidateIdentity(int id, int candidateId, String name, String email, String phone, String address) {
        this.id = id;
        this.candidateId = candidateId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
