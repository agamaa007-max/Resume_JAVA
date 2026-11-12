package com.blindhire.test;

import com.blindhire.model.*;
import com.blindhire.parser.*;
import com.blindhire.security.PasswordUtil;
import com.blindhire.security.SessionManager;
import com.blindhire.service.AnonymizationService;
import com.blindhire.service.MatchingService;
import com.blindhire.util.CandidateCodeGenerator;
import com.blindhire.util.JSONUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * BlindHireIntegrationTest verifies core backend algorithms and parsers:
 * - PBKDF2 Password Hashing & Verification
 * - Session Management
 * - Skill Extraction & Normalization
 * - Identity Redaction & Quarantine
 * - Multi-Factor Matching Algorithm (50% / 25% / 15% / 10%)
 * - PDF Resume Text Extraction (Apache PDFBox)
 * - DOCX Resume Text Extraction (Apache POI)
 */
public class BlindHireIntegrationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("       BlindHire System Verification & Unit Tests         ");
        System.out.println("==========================================================");

        testPasswordSecurity();
        testSessionManagement();
        testCandidateCodeGenerator();
        testSkillExtractor();
        testAnonymizationService();
        testMatchingAlgorithm();
        testPDFResumeParser();
        testDOCXResumeParser();
        testJSONSerialization();

        System.out.println("\n==========================================================");
        System.out.printf("Test Execution Complete: %d PASSED, %d FAILED\n", testsPassed, testsFailed);
        System.out.println("==========================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void testPasswordSecurity() {
        System.out.println("\n--- 1. Testing Password Security (PBKDF2WithHmacSHA256) ---");
        String password = "Password@123";
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        assertTrue("Generated salt is not empty", salt != null && !salt.isEmpty());
        assertTrue("Generated hash is not empty", hash != null && !hash.isEmpty());
        assertTrue("Password verification succeeds for correct password", PasswordUtil.verifyPassword(password, salt, hash));
        assertTrue("Password verification rejects incorrect password", !PasswordUtil.verifyPassword("WrongPassword", salt, hash));

        // Different salt generates different hash
        String salt2 = PasswordUtil.generateSalt();
        String hash2 = PasswordUtil.hashPassword(password, salt2);
        assertTrue("Unique salt prevents identical hashes", !hash.equals(hash2));
    }

    private static void testSessionManagement() {
        System.out.println("\n--- 2. Testing Session Management ---");
        String token = SessionManager.createSession(1, "recruiter@blindhire.com", "Alex Morgan");
        assertTrue("Session token generated", token != null && token.length() > 20);

        SessionManager.UserSession session = SessionManager.getSession(token);
        assertTrue("Session retrieved correctly", session != null && session.getUserId() == 1);
        assertTrue("Session has valid email", "recruiter@blindhire.com".equals(session.getEmail()));

        SessionManager.invalidateSession(token);
        assertTrue("Session invalidation works", SessionManager.getSession(token) == null);
    }

    private static void testCandidateCodeGenerator() {
        System.out.println("\n--- 3. Testing Candidate Code Generator ---");
        String code = CandidateCodeGenerator.generateNextCode();
        assertTrue("Candidate code format starts with CAND-", code != null && code.startsWith("CAND-"));
    }

    private static void testSkillExtractor() {
        System.out.println("\n--- 4. Testing Skill Extractor & Normalization ---");
        String resumeText = "Software Engineer with extensive experience in Core Java, JS, Python, and Structured Query Language. " +
                           "Built cloud microservices using SpringBoot and Docker deployed to AWS.";

        List<String> skills = SkillExtractor.extractSkills(resumeText);
        assertTrue("Extracted Java from 'Core Java'", skills.contains("Java"));
        assertTrue("Extracted JavaScript from 'JS'", skills.contains("JavaScript"));
        assertTrue("Extracted Python", skills.contains("Python"));
        assertTrue("Extracted SQL from 'Structured Query Language'", skills.contains("SQL"));
        assertTrue("Extracted Spring Boot from 'SpringBoot'", skills.contains("Spring Boot"));
        assertTrue("Extracted Docker", skills.contains("Docker"));
        assertTrue("Extracted AWS", skills.contains("AWS"));

        CandidateIdentity identity = SkillExtractor.extractIdentity("Amit Verma\namit.verma@example.com\n+91 9876543212\nPune, India\nTechnical Resume");
        assertTrue("Extracted Candidate Name", "Amit Verma".equalsIgnoreCase(identity.getName()));
        assertTrue("Extracted Candidate Email", "amit.verma@example.com".equalsIgnoreCase(identity.getEmail()));
        assertTrue("Extracted Candidate Phone", identity.getPhone() != null && identity.getPhone().contains("9876543212"));
    }

    private static void testAnonymizationService() {
        System.out.println("\n--- 5. Testing Anonymization Service ---");
        AnonymizationService anon = new AnonymizationService();
        CandidateIdentity identity = new CandidateIdentity(1, 101, "Rahul Sharma", "rahul@example.com", "9876543210", "Delhi");
        String rawText = "Rahul Sharma is a developer. Email: rahul@example.com, Phone: 9876543210.";

        String masked = anon.maskIdentifiableText(rawText, identity);
        assertTrue("Masked name", !masked.contains("Rahul Sharma"));
        assertTrue("Masked email", !masked.contains("rahul@example.com"));
        assertTrue("Masked phone", !masked.contains("9876543210"));
    }

    private static void testMatchingAlgorithm() {
        System.out.println("\n--- 6. Testing Explainable Multi-Factor Matching Algorithm ---");
        MatchingService matcher = new MatchingService();

        Job job = new Job();
        job.setId(1);
        job.setTitle("Java Backend Developer");
        job.setMinimumExperience(2);
        job.addSkill(new JobSkill("Java", "REQUIRED"));
        job.addSkill(new JobSkill("SQL", "REQUIRED"));
        job.addSkill(new JobSkill("Spring Boot", "REQUIRED"));
        job.addSkill(new JobSkill("Git", "REQUIRED"));
        job.addSkill(new JobSkill("Docker", "PREFERRED"));

        Candidate candidate = new Candidate();
        candidate.setId(101);
        candidate.setCandidateCode("CAND-1001");

        candidate.setSkills(Arrays.asList(
            new CandidateSkill("Java"),
            new CandidateSkill("SQL"),
            new CandidateSkill("Spring Boot"),
            new CandidateSkill("Git"),
            new CandidateSkill("Python")
        ));

        Experience exp = new Experience();
        exp.setYearsOfExperience(2.5);
        candidate.setExperience(Arrays.asList(exp));

        Education edu = new Education();
        edu.setDegree("B.Tech");
        edu.setPercentage(84.0);
        candidate.setEducation(Arrays.asList(edu));

        Project p1 = new Project();
        p1.setProjectName("Banking Portal");
        Project p2 = new Project();
        p2.setProjectName("Resume Screening Engine");
        candidate.setProjects(Arrays.asList(p1, p2));

        ScreeningResult result = matcher.calculateScreening(candidate, job);

        assertTrue("Final score calculated > 80%", result.getFinalScore() > 80.0);
        assertTrue("Skill score calculated", result.getSkillScore() > 70.0);
        assertTrue("Experience score meets/exceeds 100% for 2.5 yrs vs 2 min", result.getExperienceScore() == 100.0);
        assertTrue("Matched skills identified", result.getMatchedSkills().contains("Java"));
        assertTrue("Missing preferred skill identified", result.getMissingSkills().stream().anyMatch(s -> s.contains("Docker")));
        assertTrue("Score explanation generated", result.getScoreExplanation() != null && result.getScoreExplanation().contains("Calculated Final Match"));
    }

    private static void testPDFResumeParser() {
        System.out.println("\n--- 7. Testing PDF Text Extraction (Apache PDFBox) ---");
        try {
            File tempPdf = File.createTempFile("test_resume_", ".pdf");
            tempPdf.deleteOnExit();

            // Generate a real synthetic PDF in memory with PDFBox
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    cs.newLineAtOffset(50, 700);
                    cs.showText("Candidate Name: Priya Patel");
                    cs.newLineAtOffset(0, -20);
                    cs.showText("Technical Skills: Python, SQL, Machine Learning, Git");
                    cs.endText();
                }
                doc.save(tempPdf);
            }

            PDFResumeParser parser = new PDFResumeParser();
            String extracted = parser.extractText(tempPdf);

            assertTrue("PDF text extracted via PDFBox", extracted != null && extracted.contains("Priya Patel"));
            assertTrue("PDF contains technical skills", extracted.contains("Machine Learning"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("PDF extraction exception: " + e.getMessage(), false);
        }
    }

    private static void testDOCXResumeParser() {
        System.out.println("\n--- 8. Testing DOCX Text Extraction (Apache POI & XML Fallback) ---");
        try {
            File tempDocx = File.createTempFile("test_resume_", ".docx");
            tempDocx.deleteOnExit();

            // Create a valid minimal DOCX zip structure containing word/document.xml
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempDocx))) {
                zos.putNextEntry(new ZipEntry("word/document.xml"));
                String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                             "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">" +
                             "<w:body><w:p><w:r><w:t>Amit Verma</w:t></w:r></w:p>" +
                             "<w:p><w:r><w:t>Skills: HTML, CSS, JavaScript, Java, SQL</w:t></w:r></w:p>" +
                             "</w:body></w:document>";
                zos.write(xml.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            DOCXResumeParser parser = new DOCXResumeParser();
            String extracted = parser.extractText(tempDocx);

            assertTrue("DOCX text extracted", extracted != null && extracted.contains("Amit Verma"));
            assertTrue("DOCX contains skills", extracted.contains("JavaScript"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("DOCX extraction exception: " + e.getMessage(), false);
        }
    }

    private static void testJSONSerialization() {
        System.out.println("\n--- 9. Testing Google Gson JSON Utilities ---");
        Job job = new Job();
        job.setId(10);
        job.setTitle("Frontend Engineer");
        job.setStatus("ACTIVE");

        String json = JSONUtil.toJson(job);
        assertTrue("JSON serialized correctly", json != null && json.contains("Frontend Engineer"));

        Job deserialized = JSONUtil.fromJson(json, Job.class);
        assertTrue("JSON deserialized correctly", deserialized != null && deserialized.getId() == 10);
    }
}
