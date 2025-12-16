package com.project.cinemabackend.controller;

import com.project.cinemabackend.dto.SurveyRequest;
import com.project.cinemabackend.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import com.project.cinemabackend.service.UserEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserEmbeddingController {
    @Autowired
    private UserEmbeddingService userEmbeddingService;

    @PostMapping("/public/users/embeddings/generate")
    public ResponseEntity<String> generateAllUserEmbeddings() {
        try {
            userEmbeddingService.generateUserVectors();
            return ResponseEntity.ok("Generation completed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Generation error: " + e.getMessage());
        }
    }

    @PostMapping("/user/preference/embedding/create")
    public ResponseEntity<?> createPreferences(
            Authentication authentication,
            @RequestBody SurveyRequest surveyRequest) {

        if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Unauthenticated request to /user/preference/embedding/create endpoint");
                return ResponseEntity.status(401).build();
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = principal.getUserId();

        userEmbeddingService.createUserEmbeddingFromSurvey(userId, surveyRequest);

        return ResponseEntity.ok().build();
    }
}
