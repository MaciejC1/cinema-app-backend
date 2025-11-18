package com.project.cinemabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MovieRecommendationDTO {
    private String movieTitle;
    private double similarityScore;
}
