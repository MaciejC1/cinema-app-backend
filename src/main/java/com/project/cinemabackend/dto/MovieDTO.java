package com.project.cinemabackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MovieDTO(
        @NotNull UUID id,
        @NotBlank String title,
        @NotEmpty List<String> directors,
        @NotBlank String description,
        @NotNull int durationMinutes,
        @NotBlank String ageRating,
        @NotNull BigDecimal averageRating,
        @NotEmpty List<String> genres,
        @NotBlank String backdrop,
        @NotBlank String poster,
        @NotBlank String slug
) {
}
