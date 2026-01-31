package com.project.cinemabackend.controller;

import com.project.cinemabackend.dto.ai.AiResponse;
import com.project.cinemabackend.security.UserPrincipal;
import com.project.cinemabackend.service.AiRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Slf4j
public class AiRecommendationController {
    AiRecommendationService aiRecommendationService;

    AiRecommendationController(AiRecommendationService aiRecommendationService) {
        this.aiRecommendationService = aiRecommendationService;
    }

    @GetMapping("/public/ai/recommendation")
    public ResponseEntity<?> getRecommendation(Authentication authentication) {
        try {
            return ResponseEntity.ok(aiRecommendationService.findRecommendationByAI(authentication));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Coś poszło nie tak");
        }
    }
}
