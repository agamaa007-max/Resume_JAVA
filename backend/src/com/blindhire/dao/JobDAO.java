package com.blindhire.dao;

import com.blindhire.database.DBConnection;
import com.blindhire.model.Job;
import com.blindhire.model.JobSkill;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JobDAO manages persistence for job vacancies and associated skill requirements.
 */
public class JobDAO {

    public Job createJob(Job job) throws SQLException {
        String sql = "INSERT INTO jobs (title, description, minimum_education, minimum_experience, created_by, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, job.getTitle());
            ps.setString(2, job.getDescription());
            ps.setString(3, job.getMinimumEducation());
            ps.setInt(4, job.getMinimumExperience());
            ps.setInt(5, job.getCreatedBy());
            ps.setString(6, job.getStatus() != null ? job.getStatus() : "ACTIVE");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    job.setId(rs.getInt(1));
                }
            }
        }

        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            saveJobSkills(job.getId(), job.getSkills());
        }

        return job;
    }

    public List<Job> getAllJobs() throws SQLException {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT j.id, j.title, j.description, j.minimum_education, j.minimum_experience, " +
                     "j.created_by, j.created_at, j.status FROM jobs j ORDER BY j.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Job job = mapRowToJob(rs);
                jobs.add(job);
            }
        }

        for (Job job : jobs) {
            job.setSkills(getJobSkills(job.getId()));
        }

        return jobs;
    }

    public Job getJobById(int id) throws SQLException {
        String sql = "SELECT id, title, description, minimum_education, minimum_experience, " +
                     "created_by, created_at, status FROM jobs WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Job job = mapRowToJob(rs);
                    job.setSkills(getJobSkills(id));
                    return job;
                }
            }
        }
        return null;
    }

    public boolean updateJob(Job job) throws SQLException {
        String sql = "UPDATE jobs SET title = ?, description = ?, minimum_education = ?, " +
                     "minimum_experience = ?, status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, job.getTitle());
            ps.setString(2, job.getDescription());
            ps.setString(3, job.getMinimumEducation());
            ps.setInt(4, job.getMinimumExperience());
            ps.setString(5, job.getStatus());
            ps.setInt(6, job.getId());

            int updated = ps.executeUpdate();
            if (updated > 0 && job.getSkills() != null) {
                clearJobSkills(job.getId());
                saveJobSkills(job.getId(), job.getSkills());
            }
            return updated > 0;
        }
    }

    public boolean deleteJob(int id) throws SQLException {
        String sql = "DELETE FROM jobs WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public void saveJobSkills(int jobId, List<JobSkill> skills) throws SQLException {
        String sql = "INSERT INTO job_skills (job_id, skill_name, skill_type) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (JobSkill skill : skills) {
                ps.setInt(1, jobId);
                ps.setString(2, skill.getSkillName());
                ps.setString(3, skill.getSkillType() != null ? skill.getSkillType() : "REQUIRED");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void clearJobSkills(int jobId) throws SQLException {
        String sql = "DELETE FROM job_skills WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            ps.executeUpdate();
        }
    }

    public List<JobSkill> getJobSkills(int jobId) throws SQLException {
        List<JobSkill> list = new ArrayList<>();
        String sql = "SELECT id, job_id, skill_name, skill_type FROM job_skills WHERE job_id = ? ORDER BY skill_type DESC, skill_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new JobSkill(
                        rs.getInt("id"),
                        rs.getInt("job_id"),
                        rs.getString("skill_name"),
                        rs.getString("skill_type")
                    ));
                }
            }
        }
        return list;
    }

    private Job mapRowToJob(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setId(rs.getInt("id"));
        job.setTitle(rs.getString("title"));
        job.setDescription(rs.getString("description"));
        job.setMinimumEducation(rs.getString("minimum_education"));
        job.setMinimumExperience(rs.getInt("minimum_experience"));
        job.setCreatedBy(rs.getInt("created_by"));
        job.setCreatedAt(rs.getTimestamp("created_at"));
        job.setStatus(rs.getString("status"));
        return job;
    }
}
