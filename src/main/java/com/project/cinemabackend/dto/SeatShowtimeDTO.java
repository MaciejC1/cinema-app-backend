package com.project.cinemabackend.dto;


import java.util.UUID;

public record SeatShowtimeDTO(
        UUID id,
        Integer rowNumber,
        Integer seatNumber,
        String seatType,
        boolean isAvailable,
        boolean isAvailableForShowtime
) {
}
