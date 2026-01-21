package com.project.cinemabackend.dto;

import com.project.cinemabackend.model.BookingStatus;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record LastBookingDTO(
        CinemaMinimalDTO cinema,
        MovieMinimalDTO movie,
        OffsetDateTime updatedAt,
        BookingStatus status,
        ShowtimeDTO showtime,
        List <BookingSeatDTO>  seats,
        BigDecimal amount
) {
}
