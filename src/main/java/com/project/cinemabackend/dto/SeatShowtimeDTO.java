package com.project.cinemabackend.dto;

public record SeatShowtimeDTO(
        Integer rowNumber,
        Integer seatNumber,
        String seatType,
        boolean isAvailable,
        boolean isAvailableForShowtime
) {
}
