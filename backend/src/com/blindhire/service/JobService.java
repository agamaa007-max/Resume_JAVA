package com.blindhire.service;

import com.blindhire.dao.AuditDAO;
import com.blindhire.dao.JobDAO;
import com.blindhire.model.Job;
import com.blindhire.model.JobSkill;
import java.sql.SQLException;
import java.util.List;

/**
 * JobService provides business validation and coordination for job postings.
 */
public class JobService {

    private final JobDAO jobDAO;
    private final AuditDAO auditDAO;

    public JobService() {
        this.jobDAO = new JobDAO();
        this.auditDAO = new AuditDAO();
    }

    public JobService(JobDAO jobDAO, AuditDAO auditDAO) {
        this.jobDAO = jobDAO;
        this.auditDAO = auditDAO;
    }

    public Job createJob(Job job, int recruiterId) throws SQLException {
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Job title is required.");
        }
        if (job.getDescription() == null || job.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Job description is required.");
        }

        job.setCreatedBy(recruiterId);
        job.setStatus("ACTIVE");

        Job created = jobDAO.createJob(job);
        auditDAO.logAction(recruiterId, "CREATE_JOB", null, "Created job vacancy: " + job.getTitle() + " (ID: " + created.getId() + ")");
        return created;
    }

    public List<Job> getAllJobs() throws SQLException {
        return jobDAO.getAllJobs();
    }

    public Job getJobById(int id) throws SQLException {
        return jobDAO.getJobById(id);
    }

    public boolean updateJob(Job job, int recruiterId) throws SQLException {
        Job existing = jobDAO.getJobById(job.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Job not found.");
        }

        boolean updated = jobDAO.updateJob(job);
        if (updated) {
            auditDAO.logAction(recruiterId, "UPDATE_JOB", null, "Updated job: " + job.getTitle() + " (ID: " + job.getId() + ")");
        }
        return updated;
    }

    public boolean deleteJob(int id, int recruiterId) throws SQLException {
        Job existing = jobDAO.getJobById(id);
        if (existing == null) {
            return false;
        }

        boolean deleted = jobDAO.deleteJob(id);
        if (deleted) {
            auditDAO.logAction(recruiterId, "DELETE_JOB", null, "Deleted job vacancy ID: " + id + " (" + existing.getTitle() + ")");
        }
        return deleted;
    }

    public List<JobSkill> getJobSkills(int jobId) throws SQLException {
        return jobDAO.getJobSkills(jobId);
    }
}
