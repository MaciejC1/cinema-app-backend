package com.project.cinemabackend.recomendation_systems.service;

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

@Service
public class ContentBasedRecommendationService {

    @Autowired
    private UserEmbeddingRepository userEmbeddingRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieEmbeddingRepository movieEmbeddingRepository;

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
