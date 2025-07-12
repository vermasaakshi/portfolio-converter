package com.example.converter.dto;


public class ExperienceDto {
    private String position;
    private String company;
    private String duration;
    private String description;

    public ExperienceDto() {}

    public ExperienceDto(String position, String company, String duration, String description) {
        this.position = position;
        this.company = company;
        this.duration = duration;
        this.description = description;
    }

    // Getters and setters
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}