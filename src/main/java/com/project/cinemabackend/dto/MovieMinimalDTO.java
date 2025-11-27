package com.project.cinemabackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record MovieMinimalDTO(
        @NotNull UUID id,
        @NotBlank String title,
        @NotNull int durationMinutes,
        @NotNull BigDecimal averageRating,
        @NotBlank String poster,
        @NotBlank String slug
) {
}
