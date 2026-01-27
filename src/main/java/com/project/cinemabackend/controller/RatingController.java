package com.project.cinemabackend.controller;

import com.project.cinemabackend.dto.RatingRequest;
import com.project.cinemabackend.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RatingController {
    private final RatingService ratingService;

    RatingController(RatingService ratingService) { this.ratingService = ratingService; }

    @PostMapping("/user/movie/{movieId}")
    public ResponseEntity<?> addOrUpdateRating(
            @PathVariable UUID movieId,
            @RequestBody RatingRequest request,
            Authentication authentication
    ) {
        ratingService.addOrUpdateRating(movieId, authentication, request.value());
        return ResponseEntity.ok("Rating added successfully");
    }
}
