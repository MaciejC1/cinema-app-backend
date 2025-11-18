package com.project.cinemabackend.controller;

import com.project.cinemabackend.service.UserEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/users/embeddings")
public class UserEmbeddingController {
    @Autowired
    private UserEmbeddingService userEmbeddingService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateAllUserEmbeddings() {
        try {
            userEmbeddingService.generateUserVectors();
            return ResponseEntity.ok("Generation completed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Generation error: " + e.getMessage());
        }
    }
}
