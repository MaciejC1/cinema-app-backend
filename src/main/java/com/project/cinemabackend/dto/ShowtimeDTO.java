package com.project.cinemabackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ShowtimeDTO(
        UUID id,
        LocalDateTime startTime,
        boolean is3d,
        String language,
        String subtitles,
        String hallName
) {}
