package com.project.cinemabackend.recomendation_systems.service;

import com.project.cinemabackend.dto.MovieMatchDetailedDTO;
import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.model.UserEmbedding;
import com.project.cinemabackend.model.UserRating;
import com.project.cinemabackend.repository.MovieRepository;
import com.project.cinemabackend.repository.UserEmbeddingRepository;
import com.project.cinemabackend.repository.UserRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.clamp;

@Service
public class CollaborativeFilteringRecommendationService {

    @Autowired
    private UserEmbeddingRepository userEmbeddingRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRatingRepository userRatingRepository;

    private static final double MIN_DISPLAY = 0.5;

    public List<MovieMatchDetailedDTO> recommendAllMoviesForUser(UUID userId) {
        Optional<UserEmbedding> optionalUserEmbedding = userEmbeddingRepository.findByUser_Id(userId);
        if (optionalUserEmbedding.isEmpty()) return Collections.emptyList();

        double[] targetUserVector = optionalUserEmbedding.get().getEmbeddingVector();
        List<Movie> activeMovies = movieRepository.findAllByIsActiveTrue();

        List<MovieMatchDetailedDTO> results = new ArrayList<>();

        for (Movie movie : activeMovies) {
            List<UserRating> ratings = userRatingRepository.findAllByMovie_Id(movie.getId());

            List<UserEmbedding> ratingUserEmbeddings = ratings.stream()
                    .map(r -> userEmbeddingRepository.findByUser_Id(r.getUser().getId()).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (ratingUserEmbeddings.isEmpty()) {
                results.add(new MovieMatchDetailedDTO(
                        movie.getTitle(),
                        0.0,
                        null,
                        null,
                        null
                ));
                continue;
            }

            List<UserRatingSimilarity> similarities = new ArrayList<>();
            for (UserRating rating : ratings) {
                UserEmbedding ue = userEmbeddingRepository.findByUser_Id(rating.getUser().getId()).orElse(null);
                if (ue == null) continue;
                double sim = cosineSimilarity(targetUserVector, ue.getEmbeddingVector());
                similarities.add(new UserRatingSimilarity(rating, sim));
            }

            List<UserRatingSimilarity> topSimilarities = similarities.stream()
                    .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                    .limit(3)
                    .collect(Collectors.toList());

            double averageRating = topSimilarities.stream()
                    .mapToDouble(r -> r.rating.getRating())
                    .average()
                    .orElse(0.0);

            double normalized = clamp(MIN_DISPLAY + (averageRating / 5.0) * (1.0 - MIN_DISPLAY), 0.0, 1.0);
            String similarityPercentage = String.format("%.2f%%", (averageRating / 5.0) * 100);
            String normalizedPercentage = String.format("%.2f%%", normalized * 100);

            results.add(new MovieMatchDetailedDTO(
                    movie.getTitle(),
                    averageRating / 5.0,
                    similarityPercentage,
                    normalized,
                    normalizedPercentage
            ));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(m -> - (m.similarityNormalized() != null ? m.similarityNormalized() : 0)))
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

    private static class UserRatingSimilarity {
        private final UserRating rating;
        private final double similarity;

        public UserRatingSimilarity(UserRating rating, double similarity) {
            this.rating = rating;
            this.similarity = similarity;
        }
    }
}
