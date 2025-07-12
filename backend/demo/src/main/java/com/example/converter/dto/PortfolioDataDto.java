package com.example.converter.dto;


import java.util.List;

public class PortfolioDataDto {
    private PersonalInfoDto personalInfo;
    private List<String> skills;
    private List<ExperienceDto> experience;
    private List<EducationDto> education;

    public PortfolioDataDto() {}

    public PortfolioDataDto(PersonalInfoDto personalInfo, List<String> skills,
                          List<ExperienceDto> experience, List<EducationDto> education) {
        this.personalInfo = personalInfo;
        this.skills = skills;
        this.experience = experience;
        this.education = education;
    }

    // Getters and setters
    public PersonalInfoDto getPersonalInfo() {
        return personalInfo;
    }

    public void setPersonalInfo(PersonalInfoDto personalInfo) {
        this.personalInfo = personalInfo;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<ExperienceDto> getExperience() {
        return experience;
    }

    public void setExperience(List<ExperienceDto> experience) {
        this.experience = experience;
    }

    public List<EducationDto> getEducation() {
        return education;
    }

    public void setEducation(List<EducationDto> education) {
        this.education = education;
    }
}