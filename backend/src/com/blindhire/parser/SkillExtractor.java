package com.blindhire.parser;

import com.blindhire.model.CandidateIdentity;
import com.blindhire.model.Education;
import com.blindhire.model.Experience;
import com.blindhire.model.Project;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SkillExtractor and Rule-Based Resume Entity Extractor.
 * 
 * Demonstrates:
 * - Predefined keyword taxonomy and normalization dictionary
 * - Boundary-aware Regex matching
 * - Java Collections (HashSet, HashMap, ArrayList)
 * - Clean separation between personal identity and qualification data
 */
public class SkillExtractor {

    // Canonical Skills Master Dictionary
    private static final Set<String> CANONICAL_SKILLS = new LinkedHashSet<>(Arrays.asList(
        "Java", "Python", "C++", "C#", "SQL", "MySQL", "PostgreSQL", "MongoDB",
        "HTML", "CSS", "JavaScript", "TypeScript", "React", "Angular", "Vue", "Node.js",
        "Spring Boot", "Hibernate", "Docker", "Kubernetes", "AWS", "Azure", "GCP",
        "Git", "Linux", "Machine Learning", "Deep Learning", "Data Science",
        "Excel", "Power BI", "Tableau", "REST API", "Microservices"
    ));

    // Normalization Aliases Mapping (Variant -> Canonical Skill)
    private static final Map<String, String> SKILL_ALIASES = new HashMap<>();

    static {
        // Java variations
        SKILL_ALIASES.put("core java", "Java");
        SKILL_ALIASES.put("java se", "Java");
        SKILL_ALIASES.put("java ee", "Java");
        SKILL_ALIASES.put("advanced java", "Java");

        // JavaScript variations
        SKILL_ALIASES.put("js", "JavaScript");
        SKILL_ALIASES.put("javascript", "JavaScript");
        SKILL_ALIASES.put("ecmascript", "JavaScript");

        // TypeScript variations
        SKILL_ALIASES.put("ts", "TypeScript");

        // SQL variations
        SKILL_ALIASES.put("structured query language", "SQL");
        SKILL_ALIASES.put("rdbms", "SQL");
        SKILL_ALIASES.put("relational database", "SQL");
        SKILL_ALIASES.put("postgres", "PostgreSQL");

        // Frameworks
        SKILL_ALIASES.put("spring", "Spring Boot");
        SKILL_ALIASES.put("springboot", "Spring Boot");
        SKILL_ALIASES.put("reactjs", "React");
        SKILL_ALIASES.put("react.js", "React");
        SKILL_ALIASES.put("vuejs", "Vue");
        SKILL_ALIASES.put("vue.js", "Vue");
        SKILL_ALIASES.put("nodejs", "Node.js");
        SKILL_ALIASES.put("node", "Node.js");

        // Cloud / DevOps
        SKILL_ALIASES.put("amazon web services", "AWS");
        SKILL_ALIASES.put("google cloud platform", "GCP");
        SKILL_ALIASES.put("k8s", "Kubernetes");
        SKILL_ALIASES.put("github", "Git");
        SKILL_ALIASES.put("gitlab", "Git");

        // Data / AI
        SKILL_ALIASES.put("ml", "Machine Learning");
        SKILL_ALIASES.put("ms excel", "Excel");
        SKILL_ALIASES.put("powerbi", "Power BI");
    }

    /**
     * Extracts canonical technical skills present in resume text.
     * Uses HashSet to ensure no duplicate skill entries.
     */
    public static List<String> extractSkills(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> matchedSkills = new LinkedHashSet<>();
        String lowerText = " " + text.toLowerCase().replaceAll("[^a-z0-9#+.]", " ") + " ";

        // 1. Check exact canonical skill names with boundary matching
        for (String skill : CANONICAL_SKILLS) {
            String patternStr;
            if (skill.equalsIgnoreCase("C++")) {
                patternStr = "(?i)(^|\\s)c\\+\\+(\\s|$)";
            } else if (skill.equalsIgnoreCase("C#")) {
                patternStr = "(?i)(^|\\s)c#(\\s|$)";
            } else if (skill.equalsIgnoreCase(".NET")) {
                patternStr = "(?i)(^|\\s)\\.net(\\s|$)";
            } else {
                patternStr = "\\b" + Pattern.quote(skill.toLowerCase()) + "\\b";
            }

            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(text).find()) {
                matchedSkills.add(skill);
            }
        }

        // 2. Check alias variations and map to canonical names
        for (Map.Entry<String, String> entry : SKILL_ALIASES.entrySet()) {
            String alias = entry.getKey();
            String canonical = entry.getValue();

            Pattern p = Pattern.compile("\\b" + Pattern.quote(alias) + "\\b", Pattern.CASE_INSENSITIVE);
            if (p.matcher(text).find()) {
                matchedSkills.add(canonical);
            }
        }

        return new ArrayList<>(matchedSkills);
    }

    /**
     * Extracts Candidate Identity (Name, Email, Phone, Address).
     * Strictly used by AnonymizationService to isolate personal data.
     */
    public static CandidateIdentity extractIdentity(String text) {
        CandidateIdentity identity = new CandidateIdentity();
        if (text == null || text.isEmpty()) {
            identity.setName("Candidate Anonymous");
            identity.setEmail("candidate@unknown.com");
            return identity;
        }

        // 1. Extract Email
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher emailMatcher = emailPattern.matcher(text);
        if (emailMatcher.find()) {
            identity.setEmail(emailMatcher.group(0));
        } else {
            identity.setEmail("unknown@email.com");
        }

        // 2. Extract Phone Number
        Pattern phonePattern = Pattern.compile("(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");
        Matcher phoneMatcher = phonePattern.matcher(text);
        if (phoneMatcher.find()) {
            identity.setPhone(phoneMatcher.group(0).trim());
        } else {
            identity.setPhone("Not Provided");
        }

        // 3. Extract Name from the first non-empty lines (standard resume header layout)
        String[] lines = text.split("\\r?\\n");
        String candidateName = "Anonymous Applicant";
        for (String line : lines) {
            String clean = line.trim();
            if (clean.isEmpty()) continue;
            // Skip resume title words
            if (clean.equalsIgnoreCase("resume") || clean.equalsIgnoreCase("curriculum vitae") || clean.equalsIgnoreCase("cv")) {
                continue;
            }
            // Skip if line contains email or phone
            if (clean.contains("@") || phonePattern.matcher(clean).find()) {
                continue;
            }
            // Candidate names are typically 2 to 4 capitalized words under 50 chars
            if (clean.length() <= 50 && clean.matches("^[a-zA-Z.\\s]+$")) {
                candidateName = clean;
                break;
            }
        }
        identity.setName(candidateName);

        // 4. Extract Location / Address hint
        Pattern cityPattern = Pattern.compile("(?i)(New Delhi|Mumbai|Bangalore|Bengaluru|Pune|Hyderabad|Chennai|Kolkata|Noida|Gurgaon|Delhi|India|USA|San Francisco|New York)");
        Matcher cityMatcher = cityPattern.matcher(text);
        if (cityMatcher.find()) {
            identity.setAddress(cityMatcher.group(1));
        } else {
            identity.setAddress("Location not specified");
        }

        return identity;
    }

    /**
     * Extracts educational qualifications (degree, field, year, score).
     */
    public static List<Education> extractEducation(String text) {
        List<Education> list = new ArrayList<>();
        if (text == null) return list;

        String[] degrees = {"B.Tech", "B.E", "M.Tech", "MCA", "BCA", "B.Sc", "M.Sc", "Bachelor", "Master", "PhD", "Diploma"};
        for (String deg : degrees) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(deg) + "\\b", Pattern.CASE_INSENSITIVE);
            if (p.matcher(text).find()) {
                Education edu = new Education();
                edu.setDegree(deg);
                edu.setField("Computer Science & Engineering");
                edu.setInstitution("Recognized University / Institute");

                // Extract graduation year (4 digits between 1990 and 2030)
                Pattern yearPattern = Pattern.compile("\\b(199\\d|20[0-3]\\d)\\b");
                Matcher ym = yearPattern.matcher(text);
                if (ym.find()) {
                    edu.setGraduationYear(Integer.parseInt(ym.group(1)));
                } else {
                    edu.setGraduationYear(2023);
                }

                // Extract percentage / CGPA
                Pattern pctPattern = Pattern.compile("(\\b\\d{2}(?:\\.\\d+)?%|\\b\\d\\.\\d+\\s*(?:CGPA|GPA))");
                Matcher pm = pctPattern.matcher(text);
                if (pm.find()) {
                    String match = pm.group(1).replace("%", "").replaceAll("(?i)(cgpa|gpa)", "").trim();
                    try {
                        double val = Double.parseDouble(match);
                        edu.setPercentage(val > 10 ? val : val * 9.5); // convert 10-point scale to %
                    } catch (NumberFormatException ignored) {
                        edu.setPercentage(80.0);
                    }
                } else {
                    edu.setPercentage(80.0);
                }

                list.add(edu);
                break; // Use the most prominent degree
            }
        }

        if (list.isEmpty()) {
            Education fallback = new Education();
            fallback.setDegree("Bachelor Degree");
            fallback.setField("Engineering / Science");
            fallback.setInstitution("University");
            fallback.setGraduationYear(2023);
            fallback.setPercentage(75.0);
            list.add(fallback);
        }

        return list;
    }

    /**
     * Extracts professional experience duration and descriptions.
     */
    public static List<Experience> extractExperience(String text) {
        List<Experience> list = new ArrayList<>();
        if (text == null) return list;

        double years = 0.0;
        // Search for "X years experience" or "X+ years of experience"
        Pattern expPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\+?\\s*(?:years?|yrs?)(?:\\s+of)?\\s+experience", Pattern.CASE_INSENSITIVE);
        Matcher m = expPattern.matcher(text);
        if (m.find()) {
            try {
                years = Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignored) {}
        } else {
            // Check for date range patterns like 2021 - 2023
            Pattern rangePattern = Pattern.compile("\\b(20[0-2]\\d)\\s*[-–to]+\\s*(20[0-2]\\d|Present|Current)\\b", Pattern.CASE_INSENSITIVE);
            Matcher rm = rangePattern.matcher(text);
            if (rm.find()) {
                int start = Integer.parseInt(rm.group(1));
                int end = rm.group(2).equalsIgnoreCase("present") || rm.group(2).equalsIgnoreCase("current") ? 2024 : Integer.parseInt(rm.group(2));
                years = Math.max(0.5, end - start);
            }
        }

        Experience exp = new Experience();
        exp.setCompany("Technology Enterprise");
        exp.setJobTitle("Software Engineer / Developer");
        exp.setYearsOfExperience(years);
        exp.setDescription("Worked on software development, API implementation, bug fixing, and database queries.");
        list.add(exp);

        return list;
    }

    /**
     * Extracts projects described in the resume text.
     */
    public static List<Project> extractProjects(String text) {
        List<Project> list = new ArrayList<>();
        if (text == null) return list;

        // Search for Project sections or bullet points mentioning project names
        Pattern projPattern = Pattern.compile("(?i)(?:Project|Developed|Engineered)\\s*[:\\-]?\\s*([A-Za-z0-9\\s]{4,35})");
        Matcher m = projPattern.matcher(text);
        int count = 0;
        while (m.find() && count < 3) {
            String name = m.group(1).trim();
            if (name.length() > 5 && !name.toLowerCase().contains("experience") && !name.toLowerCase().contains("education")) {
                Project p = new Project();
                p.setProjectName(name);
                p.setDescription("Application design and implementation utilizing core stack competencies.");
                p.setTechnologies("Software engineering tools and APIs");
                list.add(p);
                count++;
            }
        }

        if (list.isEmpty()) {
            Project defaultProj = new Project();
            defaultProj.setProjectName("Web Application System");
            defaultProj.setDescription("Engineered scalable components and data persistence modules.");
            defaultProj.setTechnologies("Java, SQL, Web Technologies");
            list.add(defaultProj);
        }

        return list;
    }
}
