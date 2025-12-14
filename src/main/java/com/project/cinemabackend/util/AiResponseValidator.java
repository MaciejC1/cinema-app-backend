package com.project.cinemabackend.util;

import com.project.cinemabackend.dto.ai.AiResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AiResponseValidator {

    public static void validate(List<AiResponse> responses) {
        if (responses == null) {
            throw new IllegalArgumentException("AiResponse list is null");
        }

        if (responses.isEmpty()) {
            throw new IllegalArgumentException("AiResponse list is empty");
        }

        Set<UUID> uniqueMovieIds = new HashSet<>();

        for (AiResponse response : responses) {
            validateSingle(response);

            if (!uniqueMovieIds.add(response.movieId())) {
                throw new IllegalArgumentException(
                        "Duplicate movieId detected: " + response.movieId()
                );
            }
        }
    }

    private static void validateSingle(AiResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("AiResponse is null");
        }

        if (response.movieId() == null) {
            throw new IllegalArgumentException("movieId is null");
        }

        if (response.title() == null || response.title().isBlank()) {
            throw new IllegalArgumentException("title is null or blank");
        }

        if (response.match_percent() == null) {
            throw new IllegalArgumentException("match_percent is null");
        }

        if (response.match_percent() < 0 || response.match_percent() > 100) {
            throw new IllegalArgumentException(
                    "match_percent out of range (0–100): " + response.match_percent()
            );
        }

        if (response.predicted_rating() == null) {
            throw new IllegalArgumentException("predicted_rating is null");
        }

        if (response.predicted_rating() < 0.0 || response.predicted_rating() > 5.0) {
            throw new IllegalArgumentException(
                    "predicted_rating out of range (0–5): " + response.predicted_rating()
            );
        }
    }
}
