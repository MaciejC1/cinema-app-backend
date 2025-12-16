package com.project.cinemabackend.dto;

public record MovieMatchDetailedDTO(
        String movieTitle,
        double similarityScore,
        String similarityPercentage,
        double similarityNormalized,
        String similarityNormalizedPercentage
) {}

