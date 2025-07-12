package com.example.converter.services;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.converter.dto.EducationDto;
import com.example.converter.dto.ExperienceDto;
import com.example.converter.dto.PersonalInfoDto;
import com.example.converter.dto.PortfolioDataDto;

@Service
public class ResumeParserService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public PortfolioDataDto parseResume(String fileName) throws IOException {
        String filePath = uploadDir + File.separator + fileName;
        
        if (fileName.endsWith(".pdf")) {
            return parsePdfResume(filePath);
        } else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
            return parseWordResume(filePath);
        } else {
            throw new UnsupportedOperationException("Unsupported file format");
        }
    }

    private PortfolioDataDto parsePdfResume(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return extractDataFromText(text);
        }
    }

    private PortfolioDataDto parseWordResume(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            return extractDataFromText(text);
        }
    }

    private PortfolioDataDto extractDataFromText(String text) {
        Map<String, String> personalInfo = extractPersonalInfo(text);
        List<String> skills = extractSkills(text);
        List<Map<String, String>> education = extractEducation(text);
        List<Map<String, String>> experience = extractExperience(text);

        return new PortfolioDataDto(
            new PersonalInfoDto(
                personalInfo.get("name"),
                personalInfo.get("email"),
                personalInfo.get("phone"),
                personalInfo.get("address")
            ),
            skills,
            convertToExperienceDtoList(experience),
            convertToEducationDtoList(education)
        );
    }

    private Map<String, String> extractPersonalInfo(String text) {
        String[] lines = text.split("\n");
        String name = "";
        String email = "";
        String phone = "";
        String address = "";

        // Extract name (usually first non-empty line)
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                name = line.trim();
                break;
            }
        }

        // Extract email using regex
        Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
        Matcher emailMatcher = emailPattern.matcher(text);
        if (emailMatcher.find()) {
            email = emailMatcher.group();
        }

        // Extract phone using regex
        Pattern phonePattern = Pattern.compile("(\\+?\\d{1,3}[-\\s]?)?\\(?\\d{3}\\)?[-\\s]?\\d{3}[-\\s]?\\d{4}");
        Matcher phoneMatcher = phonePattern.matcher(text);
        if (phoneMatcher.find()) {
            phone = phoneMatcher.group();
        }

        // Extract address
        for (String line : lines) {
            if (line.toLowerCase().contains("address") ||
                line.toLowerCase().contains("street") ||
                line.toLowerCase().contains("city") ||
                (line.contains(",") && line.split(",").length >= 2)) {
                address = line.trim();
                break;
            }
        }

        return Map.of(
            "name", name.isEmpty() ? "Not Found" : name,
            "email", email.isEmpty() ? "Not Found" : email,
            "phone", phone.isEmpty() ? "Not Found" : phone,
            "address", address.isEmpty() ? "Not Found" : address
        );
    }

    private List<String> extractSkills(String text) {
        List<String> skills = new ArrayList<>();
        String[] commonSkills = {
            "Java", "Python", "JavaScript", "React", "Spring", "Spring Boot",
            "Node.js", "HTML", "CSS", "SQL", "MySQL", "PostgreSQL",
            "MongoDB", "Git", "Docker", "AWS", "Azure", "REST API",
            "Microservices", "Angular", "Vue.js", "TypeScript", "C++", "C#"
        };

        String lowerText = text.toLowerCase();
        for (String skill : commonSkills) {
            if (lowerText.contains(skill.toLowerCase())) {
                skills.add(skill);
            }
        }

        // Look for skills section
        String[] lines = text.split("\n");
        boolean inSkillsSection = false;

        for (String line : lines) {
            if (line.toLowerCase().contains("skills") ||
                line.toLowerCase().contains("technologies") ||
                line.toLowerCase().contains("technical")) {
                inSkillsSection = true;
                continue;
            }

            if (inSkillsSection && (line.toLowerCase().contains("experience") ||
                line.toLowerCase().contains("education") ||
                line.toLowerCase().contains("work"))) {
                break;
            }

            if (inSkillsSection && !line.trim().isEmpty()) {
                String[] lineSkills = line.split("[,:|•]");
                for (String skill : lineSkills) {
                    String trimmedSkill = skill.trim();
                    if (!trimmedSkill.isEmpty() && trimmedSkill.length() > 1) {
                        skills.add(trimmedSkill);
                    }
                }
            }
        }

        return skills.isEmpty() ? List.of("Skills not found") : skills;
    }

    private List<Map<String, String>> extractEducation(String text) {
        List<Map<String, String>> education = new ArrayList<>();
        String[] lines = text.split("\n");
        boolean inEducationSection = false;

        for (String line : lines) {
            if (line.toLowerCase().contains("education") ||
                line.toLowerCase().contains("academic")) {
                inEducationSection = true;
                continue;
            }

            if (inEducationSection && (line.toLowerCase().contains("experience") ||
                line.toLowerCase().contains("work") ||
                line.toLowerCase().contains("skills"))) {
                break;
            }

            if (inEducationSection && !line.trim().isEmpty()) {
                Map<String, String> edu = Map.of(
                    "degree", extractDegree(line),
                    "institution", extractInstitution(line),
                    "year", extractYear(line),
                    "gpa", "N/A"
                );
                education.add(edu);
            }
        }

        if (education.isEmpty()) {
            education.add(Map.of(
                "degree", "Education not found",
                "institution", "N/A",
                "year", "N/A",
                "gpa", "N/A"
            ));
        }

        return education;
    }

    private List<Map<String, String>> extractExperience(String text) {
        List<Map<String, String>> experience = new ArrayList<>();
        String[] lines = text.split("\n");
        boolean inExperienceSection = false;

        for (String line : lines) {
            if (line.toLowerCase().contains("experience") ||
                line.toLowerCase().contains("work") ||
                line.toLowerCase().contains("employment")) {
                inExperienceSection = true;
                continue;
            }

            if (inExperienceSection && (line.toLowerCase().contains("education") ||
                line.toLowerCase().contains("skills") ||
                line.toLowerCase().contains("projects"))) {
                break;
            }

            if (inExperienceSection && !line.trim().isEmpty()) {
                Map<String, String> exp = Map.of(
                    "position", extractPosition(line),
                    "company", extractCompany(line),
                    "duration", extractDuration(line),
                    "description", line.trim()
                );
                experience.add(exp);
            }
        }

        if (experience.isEmpty()) {
            experience.add(Map.of(
                "position", "Experience not found",
                "company", "N/A",
                "duration", "N/A",
                "description", "No experience details available"
            ));
        }

        return experience;
    }

    private List<EducationDto> convertToEducationDtoList(List<Map<String, String>> educationList) {
        List<EducationDto> result = new ArrayList<>();
        for (Map<String, String> edu : educationList) {
            result.add(new EducationDto(
                edu.get("degree"),
                edu.get("institution"),
                edu.get("year"),
                edu.get("gpa")
            ));
        }
        return result;
    }

    private List<ExperienceDto> convertToExperienceDtoList(List<Map<String, String>> experienceList) {
        List<ExperienceDto> result = new ArrayList<>();
        for (Map<String, String> exp : experienceList) {
            result.add(new ExperienceDto(
                exp.get("position"),
                exp.get("company"),
                exp.get("duration"),
                exp.get("description")
            ));
        }
        return result;
    }

    private String extractDegree(String text) {
        String[] degreeKeywords = {"Bachelor", "Master", "PhD", "B.S.", "M.S.", "B.A.", "M.A.", "B.Tech", "M.Tech"};
        for (String keyword : degreeKeywords) {
            if (text.toLowerCase().contains(keyword.toLowerCase())) {
                return text.trim();
            }
        }
        return "Degree not specified";
    }

    private String extractInstitution(String text) {
        String[] institutionKeywords = {"University", "College", "Institute", "School"};
        for (String keyword : institutionKeywords) {
            if (text.toLowerCase().contains(keyword.toLowerCase())) {
                return text.trim();
            }
        }
        return "Institution not specified";
    }

    private String extractYear(String text) {
        Pattern yearPattern = Pattern.compile("\\b(19|20)\\d{2}\\b");
        Matcher matcher = yearPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Year not specified";
    }

    private String extractPosition(String text) {
        String[] positionKeywords = {"Developer", "Engineer", "Manager", "Analyst", "Consultant", "Intern"};
        for (String keyword : positionKeywords) {
            if (text.toLowerCase().contains(keyword.toLowerCase())) {
                return text.trim();
            }
        }
        return "Position not specified";
    }

    private String extractCompany(String text) {
        return text.trim();
    }

    private String extractDuration(String text) {
        Pattern durationPattern = Pattern.compile("\\b(\\d{4})\\s*-\\s*(\\d{4}|present)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = durationPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Duration not specified";
    }
}