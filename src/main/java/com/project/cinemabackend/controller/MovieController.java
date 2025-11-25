package com.project.cinemabackend.controller;

import com.project.cinemabackend.dto.MovieDTO;
import com.project.cinemabackend.dto.MovieDetailsDTO;
import com.project.cinemabackend.dto.MovieMinimalDTO;
import com.project.cinemabackend.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) { this.movieService = movieService; }

    @GetMapping("/public/movies/active")
    public ResponseEntity<List<MovieDTO>> getMoviesByIsActive() {
        return ResponseEntity.ok(movieService.findMoviesIsActive());
    }

    @GetMapping("/public/movie/{id}")
    public ResponseEntity<MovieDetailsDTO> getMovieById(@PathVariable String id) {
        return ResponseEntity.ok(movieService.findMovieById(id));
    }

    @GetMapping("/public/movies/upcoming")
    public ResponseEntity<List<MovieMinimalDTO>> getMoviesByIsUpcoming() {
        return ResponseEntity.ok(movieService.findMinMoviesIsUpcoming());
    }
}
