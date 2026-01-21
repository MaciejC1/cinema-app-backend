package com.project.cinemabackend.dto;

import com.project.cinemabackend.model.Seat;

import java.math.BigDecimal;
import java.util.UUID;

public record BookingSeatDTO(
        SeatDTO seat,
        BigDecimal price,
        UUID ticketCode,
        byte[] qrCode
) {
}
