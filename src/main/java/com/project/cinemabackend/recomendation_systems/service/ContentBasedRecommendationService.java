package com.project.cinemabackend.recomendation_systems.service;

import com.project.cinemabackend.dto.MovieMatchDetailedDTO;
import com.project.cinemabackend.dto.MovieRecommendationDTO;
import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.model.MovieEmbedding;
import com.project.cinemabackend.model.UserEmbedding;
import com.project.cinemabackend.repository.MovieEmbeddingRepository;
import com.project.cinemabackend.repository.MovieRepository;
import com.project.cinemabackend.repository.UserEmbeddingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.Math.clamp;

@Service
public class ContentBasedRecommendationService {

    @Autowired
    private UserEmbeddingRepository userEmbeddingRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieEmbeddingRepository movieEmbeddingRepository;

    private static final double MIN_DISPLAY = 0.5;

    public List<MovieRecommendationDTO> recommendMoviesForUser(UUID userId, int topN) {
        Optional<UserEmbedding> optionalUserEmbedding = userEmbeddingRepository.findByUser_Id(userId);
        if (optionalUserEmbedding.isEmpty()) return Collections.emptyList();

        double[] userVector = optionalUserEmbedding.get().getEmbeddingVector();
        List<Movie> movies = StreamSupport.stream(movieRepository.findAll().spliterator(), false)
                .toList();

        List<MovieRecommendationDTO> recommendations = new ArrayList<>();
        for (Movie movie : movies) {
            Optional<MovieEmbedding> optionalMovieEmbedding = movieEmbeddingRepository.findByMovie_Id(movie.getId());
            if (optionalMovieEmbedding.isPresent()) {
                double[] movieVector = optionalMovieEmbedding.get().getEmbeddingVector();
                double similarity = cosineSimilarity(userVector, movieVector);
                recommendations.add(new MovieRecommendationDTO(movie.getTitle(), similarity));
            }
        }

        return recommendations.stream()
                .sorted(Comparator.comparingDouble(MovieRecommendationDTO::getSimilarityScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /*public double getUserMovieMatch(UUID userId, UUID movieId) {
        Optional<UserEmbedding> optionalUserEmbedding = userEmbeddingRepository.findByUser_Id(userId);
        Optional<MovieEmbedding> optionalMovieEmbedding = movieEmbeddingRepository.findByMovie_Id(movieId);

        if (optionalUserEmbedding.isEmpty() || optionalMovieEmbedding.isEmpty()) {
            throw new RuntimeException("User or Movie embedding not found");
        }

        double[] userVector = optionalUserEmbedding.get().getEmbeddingVector();
        double[] movieVector = optionalMovieEmbedding.get().getEmbeddingVector();

        return cosineSimilarity(userVector, movieVector);
    }*/

    public MovieMatchDetailedDTO getUserMovieMatch(UUID userId, UUID movieId) {
        Optional<UserEmbedding> optionalUserEmbedding = userEmbeddingRepository.findByUser_Id(userId);
        Optional<MovieEmbedding> optionalMovieEmbedding = movieEmbeddingRepository.findByMovie_Id(movieId);

        if (optionalUserEmbedding.isEmpty() || optionalMovieEmbedding.isEmpty()) {
            throw new RuntimeException("User or Movie embedding not found");
        }

        double[] userVector = optionalUserEmbedding.get().getEmbeddingVector();
        double[] targetMovieVector = optionalMovieEmbedding.get().getEmbeddingVector();
        String movieTitle = optionalMovieEmbedding.get().getMovie().getTitle();

        double similarityScore = cosineSimilarity(userVector, targetMovieVector);

        List<Movie> activeMovies = movieRepository.findAllByIsActiveTrue();
        List<Double> allSimilarities = activeMovies.stream()
                .map(Movie::getMovieEmbedding)
                .filter(Objects::nonNull)
                .map(me -> cosineSimilarity(userVector, me.getEmbeddingVector()))
                .toList();

        double min = allSimilarities.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = allSimilarities.stream().mapToDouble(Double::doubleValue).max().orElse(1);

        double similarityNormalized;

        if (max == min) {
            similarityNormalized = 1.0;
        } else {
            similarityNormalized = MIN_DISPLAY
                    + ((similarityScore - min) / (max - min)) * (1.0 - MIN_DISPLAY);
        }

        similarityNormalized = clamp(similarityNormalized, 0.0, 1.0);

        String similarityPercentage = String.format("%.2f%%", similarityScore * 100);
        String normalizedPercentage = String.format("%.2f%%", similarityNormalized * 100);

        return new MovieMatchDetailedDTO(
                movieTitle,
                similarityScore,
                similarityPercentage,
                similarityNormalized,
                normalizedPercentage
        );
    }

    public List<MovieMatchDetailedDTO> recommendAllMoviesForUser(UUID userId) {
        Optional<UserEmbedding> optionalUserEmbedding = userEmbeddingRepository.findByUser_Id(userId);
        if (optionalUserEmbedding.isEmpty()) return Collections.emptyList();

        double[] userVector = optionalUserEmbedding.get().getEmbeddingVector();
        List<Movie> activeMovies = movieRepository.findAllByIsActiveTrue();

        List<Double> allSimilarities = activeMovies.stream()
                .map(Movie::getMovieEmbedding)
                .filter(Objects::nonNull)
                .map(me -> cosineSimilarity(userVector, me.getEmbeddingVector()))
                .toList();

        double min = allSimilarities.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = allSimilarities.stream().mapToDouble(Double::doubleValue).max().orElse(1);

        List<MovieMatchDetailedDTO> results = new ArrayList<>();

        for (Movie movie : activeMovies) {
            MovieEmbedding me = movie.getMovieEmbedding();
            if (me == null) continue;

            double similarityScore = cosineSimilarity(userVector, me.getEmbeddingVector());
            double similarityNormalized = (max == min) ? 1.0 :
                    MIN_DISPLAY + ((similarityScore - min) / (max - min)) * (1.0 - MIN_DISPLAY);
            similarityNormalized = clamp(similarityNormalized, 0.0, 1.0);

            results.add(new MovieMatchDetailedDTO(
                    movie.getTitle(),
                    similarityScore,
                    String.format("%.2f%%", similarityScore * 100),
                    similarityNormalized,
                    String.format("%.2f%%", similarityNormalized * 100)
            ));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(MovieMatchDetailedDTO::similarityScore).reversed())
                .toList();
    }

    private double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) throw new IllegalArgumentException("Vectors must be the same size");
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (normA == 0 || normB == 0) ? 0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
