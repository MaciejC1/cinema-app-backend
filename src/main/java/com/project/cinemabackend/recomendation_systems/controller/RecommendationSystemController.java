package com.project.cinemabackend.recomendation_systems.controller;

import com.project.cinemabackend.dto.MovieRecommendationDTO;
import com.project.cinemabackend.dto.RecommendationRequest;
import com.project.cinemabackend.recomendation_systems.service.ContentBasedRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/recommendation")
public class RecommendationSystemController {

    @Autowired
    private ContentBasedRecommendationService recommendationService;

    @PostMapping("/user")
    public List<MovieRecommendationDTO> getUserRecommendations(@RequestBody RecommendationRequest request) {
        return recommendationService.recommendMoviesForUser(request.getUserId(), request.getTopN());
    }
}
