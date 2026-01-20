package com.project.cinemabackend.dto;

public record MovieMatchDetailedDTO(
        String movieTitle,
        Double similarityScore,
        String similarityPercentage,
        Double similarityNormalized,
        String similarityNormalizedPercentage
) {}

