package com.project.cinemabackend.dto;

public record SeatDTO(
        Integer rowNumber,
        Integer seatNumber,
        String seatType
) {
}
