package com.project.cinemabackend.recomendation_systems.controller;

import com.project.cinemabackend.dto.MovieMatchDetailedDTO;
import com.project.cinemabackend.dto.MovieRecommendationDTO;
import com.project.cinemabackend.dto.RecommendationRequest;
import com.project.cinemabackend.recomendation_systems.service.CollaborativeFilteringRecommendationService;
import com.project.cinemabackend.recomendation_systems.service.ContentBasedRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/recommendation")
public class RecommendationSystemController {

    @Autowired
    private ContentBasedRecommendationService recommendationService;
    @Autowired
    private CollaborativeFilteringRecommendationService collaborativeFilteringRecommendationService;

    @PostMapping("/user")
    public List<MovieRecommendationDTO> getUserRecommendations(@RequestBody RecommendationRequest request) {
        return recommendationService.recommendMoviesForUser(request.getUserId(), request.getTopN());
    }

    @GetMapping("/match")
    public ResponseEntity<MovieMatchDetailedDTO> getUserMovieMatch(
            @RequestParam UUID userId,
            @RequestParam UUID movieId) {

        try {
            MovieMatchDetailedDTO matchDTO = recommendationService.getUserMovieMatch(userId, movieId);
            return ResponseEntity.ok(matchDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/content-based/all")
    public ResponseEntity<List<MovieMatchDetailedDTO>> getAllContentBasedMatches(@RequestParam UUID userId) {
        List<MovieMatchDetailedDTO> matches = recommendationService.recommendAllMoviesForUser(userId);
        return ResponseEntity.ok(matches);
    }
    @GetMapping("/collaborative-filtering/all")
    public ResponseEntity<List<MovieMatchDetailedDTO>> getAllCollaborativeFilteringMatches(@RequestParam UUID userId) {
        List<MovieMatchDetailedDTO> matches = collaborativeFilteringRecommendationService.recommendAllMoviesForUser(userId);
        return ResponseEntity.ok(matches);
    }


}


