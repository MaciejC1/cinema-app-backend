package com.project.cinemabackend.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MovieDetailsDTO(
        @NotNull UUID id,
        @NotBlank String title,
        String originalTitle,
        @NotBlank String description,
        @NotNull Integer durationMinutes,
        @NotNull LocalDate releaseDate,
        @NotBlank String ageRating,
        @NotBlank String language,
        @NotBlank String country,
        @NotNull BigDecimal averageRating,
        @NotNull Integer ratingCount,
        Boolean hasSubtitles,
        Boolean hasLector,
        Boolean hasDubbing,
        Boolean isOriginalLanguage,
        @NotEmpty List<String> directors,
        @NotEmpty List<String> genres,
        @NotEmpty List<MediaDTO> media
) {}
