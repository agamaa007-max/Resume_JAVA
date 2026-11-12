package com.blindhire.service;

import com.blindhire.model.*;
import java.util.*;

/**
 * MatchingService implements the explainable multi-factor screening algorithm.
 * 
 * Formula:
 * Final Score = (Skill Score * 0.50) + (Experience Score * 0.25) + (Education Score * 0.15) + (Project Score * 0.10)
 * 
 * Demonstrates:
 * - Deterministic, rule-based screening calculations
 * - Transparent matched and missing skills tracking
 * - Clear human-in-the-loop decision-support explanations
 */
public class MatchingService {

    public static final double SKILL_WEIGHT = 0.50;
    public static final double EXPERIENCE_WEIGHT = 0.25;
    public static final double EDUCATION_WEIGHT = 0.15;
    public static final double PROJECT_WEIGHT = 0.10;

    public ScreeningResult calculateScreening(Candidate candidate, Job job) {
        if (candidate == null || job == null) {
            throw new IllegalArgumentException("Candidate and Job cannot be null");
        }

        List<JobSkill> jobSkills = job.getSkills() != null ? job.getSkills() : Collections.emptyList();
        List<CandidateSkill> candidateSkills = candidate.getSkills() != null ? candidate.getSkills() : Collections.emptyList();

        Set<String> candidateSkillSet = new HashSet<>();
        for (CandidateSkill cs : candidateSkills) {
            if (cs.getSkillName() != null) {
                candidateSkillSet.add(cs.getSkillName().trim().toLowerCase());
            }
        }

        // 1. Calculate Skill Score (50%)
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        List<JobSkill> requiredList = new ArrayList<>();
        List<JobSkill> preferredList = new ArrayList<>();

        for (JobSkill js : jobSkills) {
            if ("PREFERRED".equalsIgnoreCase(js.getSkillType())) {
                preferredList.add(js);
            } else {
                requiredList.add(js);
            }
        }

        int reqMatched = 0;
        for (JobSkill js : requiredList) {
            if (candidateSkillSet.contains(js.getSkillName().trim().toLowerCase())) {
                matchedSkills.add(js.getSkillName());
                reqMatched++;
            } else {
                missingSkills.add(js.getSkillName() + " (Required)");
            }
        }

        int prefMatched = 0;
        for (JobSkill js : preferredList) {
            if (candidateSkillSet.contains(js.getSkillName().trim().toLowerCase())) {
                matchedSkills.add(js.getSkillName() + " (Preferred)");
                prefMatched++;
            } else {
                missingSkills.add(js.getSkillName() + " (Preferred)");
            }
        }

        double skillScore;
        if (jobSkills.isEmpty()) {
            skillScore = 80.0; // Default when no skills specified for job
        } else {
            double reqRatio = requiredList.isEmpty() ? 1.0 : ((double) reqMatched / requiredList.size());
            double prefRatio = preferredList.isEmpty() ? 1.0 : ((double) prefMatched / preferredList.size());
            skillScore = (reqRatio * 80.0) + (prefRatio * 20.0);
        }

        // 2. Calculate Experience Score (25%)
        double totalExpYears = 0.0;
        if (candidate.getExperience() != null) {
            for (Experience exp : candidate.getExperience()) {
                totalExpYears += exp.getYearsOfExperience();
            }
        }

        int minExp = job.getMinimumExperience();
        double experienceScore;
        if (minExp <= 0) {
            experienceScore = 100.0;
        } else if (totalExpYears >= minExp) {
            experienceScore = Math.min(100.0, 100.0 + ((totalExpYears - minExp) * 5.0)); // bonus up to 100
            experienceScore = Math.min(100.0, experienceScore);
        } else {
            experienceScore = (totalExpYears / minExp) * 100.0;
        }

        // 3. Calculate Education Score (15%)
        double educationScore = 75.0; // Baseline
        if (candidate.getEducation() != null && !candidate.getEducation().isEmpty()) {
            Education topEdu = candidate.getEducation().get(0);
            String deg = topEdu.getDegree() != null ? topEdu.getDegree().toUpperCase() : "";

            if (deg.contains("M.TECH") || deg.contains("PHD") || deg.contains("MASTER")) {
                educationScore = 100.0;
            } else if (deg.contains("B.TECH") || deg.contains("B.E") || deg.contains("MCA")) {
                educationScore = 95.0;
            } else if (deg.contains("BCA") || deg.contains("B.SC")) {
                educationScore = 80.0;
            } else {
                educationScore = 70.0;
            }

            // Academic performance modifier if present
            if (topEdu.getPercentage() > 80.0) {
                educationScore = Math.min(100.0, educationScore + 5.0);
            }
        }

        // 4. Calculate Project Score (10%)
        int projectCount = candidate.getProjects() != null ? candidate.getProjects().size() : 0;
        double projectScore;
        if (projectCount >= 3) {
            projectScore = 95.0;
        } else if (projectCount == 2) {
            projectScore = 85.0;
        } else if (projectCount == 1) {
            projectScore = 70.0;
        } else {
            projectScore = 50.0;
        }

        // 5. Final Weighted Score
        double finalScore = (skillScore * SKILL_WEIGHT) +
                            (experienceScore * EXPERIENCE_WEIGHT) +
                            (educationScore * EDUCATION_WEIGHT) +
                            (projectScore * PROJECT_WEIGHT);

        finalScore = Math.round(finalScore * 100.0) / 100.0; // round to 2 decimals

        // 6. Build Explainability Breakdown
        StringBuilder explanation = new StringBuilder();
        explanation.append(String.format("Calculated Final Match: %.2f%%\n", finalScore));
        explanation.append(String.format("- Skills Match (50%% Weight): %.1f%% (Matched %d of %d required, %d of %d preferred)\n",
                skillScore, reqMatched, requiredList.size(), prefMatched, preferredList.size()));
        explanation.append(String.format("- Experience Match (25%% Weight): %.1f%% (Total %.1f years vs %d years required)\n",
                experienceScore, totalExpYears, minExp));
        explanation.append(String.format("- Education Match (15%% Weight): %.1f%%\n", educationScore));
        explanation.append(String.format("- Projects Match (10%% Weight): %.1f%% (%d relevant projects evaluated)\n",
                projectScore, projectCount));
        explanation.append("\nNote: This score is a screening decision-support aid and requires human recruiter review.");

        ScreeningResult result = new ScreeningResult();
        result.setCandidateId(candidate.getId());
        result.setJobId(job.getId());
        result.setSkillScore(Math.round(skillScore * 10.0) / 10.0);
        result.setExperienceScore(Math.round(experienceScore * 10.0) / 10.0);
        result.setEducationScore(Math.round(educationScore * 10.0) / 10.0);
        result.setProjectScore(Math.round(projectScore * 10.0) / 10.0);
        result.setFinalScore(finalScore);
        result.setCandidateCode(candidate.getCandidateCode());
        result.setJobTitle(job.getTitle());
        result.setMatchedSkills(matchedSkills);
        result.setMissingSkills(missingSkills);
        result.setScoreExplanation(explanation.toString());

        return result;
    }
}
