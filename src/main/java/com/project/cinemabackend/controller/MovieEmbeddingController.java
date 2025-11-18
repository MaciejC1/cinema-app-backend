package com.project.cinemabackend.controller;

import com.project.cinemabackend.service.MovieEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/movies/embeddings")
public class MovieEmbeddingController {
    @Autowired
    private MovieEmbeddingService movieEmbeddingService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateAllEmbeddings() {
        try {
            movieEmbeddingService.generateEmbeddingsForAllMovies();
            return ResponseEntity.ok("Generation completed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Generation error: " + e.getMessage());
        }
    }
}
