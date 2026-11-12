package com.blindhire.service;

import com.blindhire.dao.AuditDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * AuditService provides system-wide security auditing and compliance logging.
 */
public class AuditService {

    private final AuditDAO auditDAO;

    public AuditService() {
        this.auditDAO = new AuditDAO();
    }

    public AuditService(AuditDAO auditDAO) {
        this.auditDAO = auditDAO;
    }

    public void log(Integer userId, String action, Integer candidateId, String description) {
        auditDAO.logAction(userId, action, candidateId, description);
    }

    public List<Map<String, Object>> getAuditLogs(int limit) throws SQLException {
        return auditDAO.getAllLogs(limit);
    }
}
