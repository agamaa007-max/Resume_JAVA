package com.blindhire.service;

import com.blindhire.dao.AuditDAO;
import com.blindhire.dao.CandidateDAO;
import com.blindhire.model.Candidate;
import com.blindhire.model.CandidateIdentity;
import java.sql.SQLException;

/**
 * IdentityService manages authorized revelation of candidate identities.
 * Enforces server-side auditing and reason logging.
 */
public class IdentityService {

    private final CandidateDAO candidateDAO;
    private final AuditDAO auditDAO;

    public IdentityService() {
        this.candidateDAO = new CandidateDAO();
        this.auditDAO = new AuditDAO();
    }

    public IdentityService(CandidateDAO candidateDAO, AuditDAO auditDAO) {
        this.candidateDAO = candidateDAO;
        this.auditDAO = auditDAO;
    }

    /**
     * Authorizes and reveals identity for a candidate with mandatory reason logging.
     * 
     * @param candidateId Target candidate ID
     * @param recruiterId Authenticated recruiter ID
     * @param recruiterEmail Recruiter email for audit entry
     * @param reason Recruiter's stated justification for de-anonymization
     * @return CandidateIdentity personal records
     */
    public CandidateIdentity revealIdentity(int candidateId, int recruiterId, String recruiterEmail, String reason) throws SQLException {
        if (reason == null || reason.trim().length() < 5) {
            throw new IllegalArgumentException("A valid justification reason (at least 5 characters) is required to reveal candidate identity.");
        }

        Candidate candidate = candidateDAO.getCandidateById(candidateId);
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate not found.");
        }

        CandidateIdentity identity = candidateDAO.getCandidateIdentity(candidateId);
        if (identity == null) {
            throw new IllegalStateException("Identity records for this candidate could not be found.");
        }

        // Record audit trail in database
        String auditDesc = String.format("Identity revealed by %s. Justification: \"%s\"", recruiterEmail, reason.trim());
        auditDAO.logAction(recruiterId, "IDENTITY_REVEAL", candidateId, auditDesc);

        System.out.println("[SECURITY-AUDIT] " + auditDesc + " for " + candidate.getCandidateCode());
        return identity;
    }
}
