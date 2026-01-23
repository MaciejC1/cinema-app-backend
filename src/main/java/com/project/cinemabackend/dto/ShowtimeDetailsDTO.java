package com.project.cinemabackend.dto;

import com.project.cinemabackend.model.AudioTrackType;
import com.project.cinemabackend.model.Seat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ShowtimeDetailsDTO(
        UUID id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean is3d,
        String language,
        String subtitles,
        boolean hasSubtitles,
        AudioTrackType audioTrack,
        String screenType,
        String hallName,
        CinemaMinimalDTO cinema,
        MovieMinimalDTO movie,
        List<SeatShowtimeDTO> seats,
        BigDecimal basePrice,
        BigDecimal vipPrice,
        BigDecimal premiumPrice
) {
}
