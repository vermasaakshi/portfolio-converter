package com.example.converter.dto;


public class EducationDto {
    private String degree;
    private String institution;
    private String year;
    private String gpa;

    public EducationDto() {}

    public EducationDto(String degree, String institution, String year, String gpa) {
        this.degree = degree;
        this.institution = institution;
        this.year = year;
        this.gpa = gpa;
    }

    // Getters and setters
    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGpa() {
        return gpa;
    }

    public void setGpa(String gpa) {
        this.gpa = gpa;
    }
}