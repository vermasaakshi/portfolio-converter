package com.example.converter.controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.converter.dto.PortfolioDataDto;
import com.example.converter.services.FileStorageService;
import com.example.converter.services.ResumeParserService;

@RestController
@CrossOrigin("*")
public class PortfolioController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ResumeParserService resumeParserService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = fileStorageService.storeFile(file);
            return ResponseEntity.ok().body(Map.of(
                "message", "File uploaded successfully",
                "fileName", fileName
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Upload failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/parse")
    public ResponseEntity<?> parseResume(@RequestBody Map<String, String> request) {
        try {
            String fileName = request.get("fileName");
            PortfolioDataDto parsedData = resumeParserService.parseResume(fileName);
            return ResponseEntity.ok().body(parsedData);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Parsing failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateWebsite(@RequestBody PortfolioDataDto data) {
        try {
            // Your website generation logic here
            String websiteUrl = generatePortfolioWebsite(data);
            return ResponseEntity.ok().body(Map.of(
                "websiteUrl", websiteUrl,
                "message", "Website generated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Website generation failed: " + e.getMessage()
            ));
        }
    }

    private String generatePortfolioWebsite(PortfolioDataDto data) {
        // Implement your website generation logic here
        // This should return the URL of the generated website
        return "http://localhost:8080/portfolio/" + System.currentTimeMillis();
    }
}