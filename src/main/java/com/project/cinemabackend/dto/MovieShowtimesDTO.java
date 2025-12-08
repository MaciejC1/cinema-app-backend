package com.project.cinemabackend.dto;

import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;

public record MovieShowtimesDTO(
        UUID id,
        String title,
        String poster,
        String ageRating,
        List<String> genres,
        List<ShowtimeDTO> showtimes
) {}