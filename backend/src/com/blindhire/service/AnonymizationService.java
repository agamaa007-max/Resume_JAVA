package com.blindhire.service;

import com.blindhire.model.Candidate;
import com.blindhire.model.CandidateIdentity;
import com.blindhire.util.CandidateCodeGenerator;

/**
 * AnonymizationService ensures full privacy isolation:
 * - Generates unique synthetic candidate codes (e.g. CAND-1001)
 * - Isolates personally identifiable information (PII) from screening profiles
 * - Guarantees that scoring and ranking are strictly blind
 */
public class AnonymizationService {

    /**
     * Generates a new unique anonymous candidate code.
     */
    public String generateAnonymousCode() {
        return CandidateCodeGenerator.generateNextCode();
    }

    /**
     * Creates an anonymous candidate entity from raw code.
     */
    public Candidate createAnonymousCandidate() {
        String code = generateAnonymousCode();
        Candidate candidate = new Candidate();
        candidate.setCandidateCode(code);
        candidate.setStatus("APPLIED");
        return candidate;
    }

    /**
     * Strips common identifying personal details from raw resume text
     * before any text is shown in candidate inspection views.
     */
    public String maskIdentifiableText(String rawText, CandidateIdentity identity) {
        if (rawText == null) return "";
        String masked = rawText;

        if (identity != null) {
            if (identity.getName() != null && identity.getName().length() > 2) {
                masked = masked.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(identity.getName()) + "\\b", "[REDACTED NAME]");
            }
            if (identity.getEmail() != null) {
                masked = masked.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(identity.getEmail()) + "\\b", "[REDACTED EMAIL]");
            }
            if (identity.getPhone() != null && identity.getPhone().length() > 5) {
                masked = masked.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(identity.getPhone()) + "\\b", "[REDACTED PHONE]");
            }
        }

        // Mask any remaining email addresses or phone patterns
        masked = masked.replaceAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "[REDACTED EMAIL]");
        masked = masked.replaceAll("(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}", "[REDACTED PHONE]");

        return masked;
    }
}
