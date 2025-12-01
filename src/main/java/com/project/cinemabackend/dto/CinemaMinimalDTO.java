package com.project.cinemabackend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CinemaMinimalDTO(
        @NotNull UUID id,
        String name,
        String address,
        String city,
        String postalCode,
        String country
) {
}
