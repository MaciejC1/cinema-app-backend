package com.project.cinemabackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
@Getter
@Setter
public class SurveyRequest {
    private List<String> favouriteGenres;
    private List<MovieRating> ratings;

    @Getter
    @Setter
    public static class MovieRating {
        private UUID movieId;
        private double rating;
    }
}
