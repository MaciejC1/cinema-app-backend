package com.project.cinemabackend.dto;

import org.hibernate.validator.constraints.Range;

public record RatingRequest(
        @Range(min = 1, max = 5)
        Integer value
) {
}
