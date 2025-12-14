package com.project.cinemabackend.dto;

import java.util.UUID;

public record MovieSurveyDTO(
        UUID id,
        String title,
        String poster,
        String slug
) {
}
