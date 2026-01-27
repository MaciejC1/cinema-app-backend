package com.project.cinemabackend.controller;

import com.project.cinemabackend.dto.*;
import com.project.cinemabackend.service.MovieService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/public/movies/active")
    public ResponseEntity<List<MovieDTO>> getMoviesByIsActive() {
        return ResponseEntity.ok(movieService.findMoviesIsActive());
    }

    @GetMapping("/public/movie/id/{id}")
    public ResponseEntity<MovieDetailsDTO> getMovieById(@PathVariable UUID id) {
        return ResponseEntity.ok(movieService.findMovieById(id));
    }

    @GetMapping("/public/movie/slug/{slug}")
    public ResponseEntity<MovieDetailsDTO> getMovieBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(movieService.findMovieBySlug(slug));
    }

    @GetMapping("/public/movies/upcoming")
    public ResponseEntity<List<MovieMinimalDTO>> getMoviesByIsUpcoming() {
        return ResponseEntity.ok(movieService.findMinMoviesIsUpcoming());
    }

    @GetMapping("/public/movies/showtimes")
    public ResponseEntity<List<MovieShowtimesDTO>> getFilteredMoviesWithShowtimes(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(value = "genre", required = false)
            String genre,
            @RequestParam("cinemaId")
            UUID cinemaId
    ) {
        return ResponseEntity.ok(movieService.getMoviesForDateAndGenres(date, genre, cinemaId));
    }

    @GetMapping("/public/movie/showtimes")
    public ResponseEntity<MovieShowtimesDTO> getMovieWithShowtimes(
            @RequestParam(value = "slug", required = false)
            String slug,
            @RequestParam("cinemaId")
            UUID cinemaId
    ) {
        return ResponseEntity.ok(movieService.getMovieForSlug(slug, cinemaId));
    }

    @GetMapping("/public/movies/survey")
    public ResponseEntity<List<MovieSurveyDTO>> getAllMovies() {
        return ResponseEntity.ok(movieService.getMoviesIsRecommended());
    }

    @GetMapping("/user/movies/rating")
    public ResponseEntity<List<RatingMovieDTO>> getAllMoviesWithRatingByUser(Authentication authentication) {
        return ResponseEntity.ok(movieService.getMoviesWithRatingByUser(authentication));
    }
}
