package com.project.cinemabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingMovieDTO {
    MovieMinimalDTO movieMinimalDTO;
    int rating;
}
