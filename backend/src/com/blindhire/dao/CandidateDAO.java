package com.blindhire.dao;

import com.blindhire.database.DBConnection;
import com.blindhire.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CandidateDAO handles persistence for candidates, educational records,
 * experience history, technical projects, skills, and isolated identity data.
 */
public class CandidateDAO {

    public Candidate saveCandidate(Candidate candidate) throws SQLException {
        String sql = "INSERT INTO candidates (candidate_code, status) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, candidate.getCandidateCode());
            ps.setString(2, candidate.getStatus() != null ? candidate.getStatus() : "APPLIED");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    candidate.setId(rs.getInt(1));
                }
            }
        }
        return candidate;
    }

    public List<Candidate> getAllCandidates() throws SQLException {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT id, candidate_code, created_at, status FROM candidates ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Candidate c = new Candidate(
                    rs.getInt("id"),
                    rs.getString("candidate_code"),
                    rs.getTimestamp("created_at"),
                    rs.getString("status")
                );
                list.add(c);
            }
        }

        for (Candidate c : list) {
            c.setSkills(getCandidateSkills(c.getId()));
            c.setEducation(getEducation(c.getId()));
            c.setExperience(getExperience(c.getId()));
            c.setProjects(getProjects(c.getId()));
        }

        return list;
    }

    public Candidate getCandidateById(int id) throws SQLException {
        String sql = "SELECT id, candidate_code, created_at, status FROM candidates WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Candidate c = new Candidate(
                        rs.getInt("id"),
                        rs.getString("candidate_code"),
                        rs.getTimestamp("created_at"),
                        rs.getString("status")
                    );
                    c.setSkills(getCandidateSkills(id));
                    c.setEducation(getEducation(id));
                    c.setExperience(getExperience(id));
                    c.setProjects(getProjects(id));
                    return c;
                }
            }
        }
        return null;
    }

    public Candidate getCandidateByCode(String code) throws SQLException {
        String sql = "SELECT id, candidate_code, created_at, status FROM candidates WHERE candidate_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    Candidate c = new Candidate(
                        id,
                        rs.getString("candidate_code"),
                        rs.getTimestamp("created_at"),
                        rs.getString("status")
                    );
                    c.setSkills(getCandidateSkills(id));
                    c.setEducation(getEducation(id));
                    c.setExperience(getExperience(id));
                    c.setProjects(getProjects(id));
                    return c;
                }
            }
        }
        return null;
    }

    public boolean updateCandidateStatus(int candidateId, String status) throws SQLException {
        String sql = "UPDATE candidates SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, candidateId);
            return ps.executeUpdate() > 0;
        }
    }

    // --- Isolated Identity Methods ---

    public void saveCandidateIdentity(CandidateIdentity identity) throws SQLException {
        String sql = "INSERT INTO candidate_identity (candidate_id, name, email, phone, address) " +
                     "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), " +
                     "phone=VALUES(phone), address=VALUES(address)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, identity.getCandidateId());
            ps.setString(2, identity.getName());
            ps.setString(3, identity.getEmail());
            ps.setString(4, identity.getPhone());
            ps.setString(5, identity.getAddress());
            ps.executeUpdate();
        }
    }

    public CandidateIdentity getCandidateIdentity(int candidateId) throws SQLException {
        String sql = "SELECT id, candidate_id, name, email, phone, address FROM candidate_identity WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CandidateIdentity(
                        rs.getInt("id"),
                        rs.getInt("candidate_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                    );
                }
            }
        }
        return null;
    }

    // --- Skills ---

    public void saveCandidateSkills(int candidateId, List<String> skills) throws SQLException {
        String sql = "INSERT INTO candidate_skills (candidate_id, skill_name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (String skill : skills) {
                ps.setInt(1, candidateId);
                ps.setString(2, skill);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<CandidateSkill> getCandidateSkills(int candidateId) throws SQLException {
        List<CandidateSkill> list = new ArrayList<>();
        String sql = "SELECT id, candidate_id, skill_name FROM candidate_skills WHERE candidate_id = ? ORDER BY skill_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CandidateSkill(rs.getInt("id"), rs.getInt("candidate_id"), rs.getString("skill_name")));
                }
            }
        }
        return list;
    }

    // --- Education ---

    public void saveEducation(Education edu) throws SQLException {
        String sql = "INSERT INTO candidate_education (candidate_id, degree, institution, field, graduation_year, percentage) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, edu.getCandidateId());
            ps.setString(2, edu.getDegree());
            ps.setString(3, edu.getInstitution());
            ps.setString(4, edu.getField());
            ps.setInt(5, edu.getGraduationYear());
            ps.setDouble(6, edu.getPercentage());
            ps.executeUpdate();
        }
    }

    public List<Education> getEducation(int candidateId) throws SQLException {
        List<Education> list = new ArrayList<>();
        String sql = "SELECT id, candidate_id, degree, institution, field, graduation_year, percentage " +
                     "FROM candidate_education WHERE candidate_id = ? ORDER BY graduation_year DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Education(
                        rs.getInt("id"),
                        rs.getInt("candidate_id"),
                        rs.getString("degree"),
                        rs.getString("institution"),
                        rs.getString("field"),
                        rs.getInt("graduation_year"),
                        rs.getDouble("percentage")
                    ));
                }
            }
        }
        return list;
    }

    // --- Experience ---

    public void saveExperience(Experience exp) throws SQLException {
        String sql = "INSERT INTO candidate_experience (candidate_id, company, job_title, years_of_experience, description) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, exp.getCandidateId());
            ps.setString(2, exp.getCompany());
            ps.setString(3, exp.getJobTitle());
            ps.setDouble(4, exp.getYearsOfExperience());
            ps.setString(5, exp.getDescription());
            ps.executeUpdate();
        }
    }

    public List<Experience> getExperience(int candidateId) throws SQLException {
        List<Experience> list = new ArrayList<>();
        String sql = "SELECT id, candidate_id, company, job_title, years_of_experience, description " +
                     "FROM candidate_experience WHERE candidate_id = ? ORDER BY years_of_experience DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Experience(
                        rs.getInt("id"),
                        rs.getInt("candidate_id"),
                        rs.getString("company"),
                        rs.getString("job_title"),
                        rs.getDouble("years_of_experience"),
                        rs.getString("description")
                    ));
                }
            }
        }
        return list;
    }

    // --- Projects ---

    public void saveProject(Project proj) throws SQLException {
        String sql = "INSERT INTO candidate_projects (candidate_id, project_name, description, technologies) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, proj.getCandidateId());
            ps.setString(2, proj.getProjectName());
            ps.setString(3, proj.getDescription());
            ps.setString(4, proj.getTechnologies());
            ps.executeUpdate();
        }
    }

    public List<Project> getProjects(int candidateId) throws SQLException {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT id, candidate_id, project_name, description, technologies " +
                     "FROM candidate_projects WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Project(
                        rs.getInt("id"),
                        rs.getInt("candidate_id"),
                        rs.getString("project_name"),
                        rs.getString("description"),
                        rs.getString("technologies")
                    ));
                }
            }
        }
        return list;
    }
}
