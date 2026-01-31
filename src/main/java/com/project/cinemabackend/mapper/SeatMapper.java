package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.SeatDTO;
import com.project.cinemabackend.dto.SeatShowtimeDTO;
import com.project.cinemabackend.model.BookingSeat;
import com.project.cinemabackend.model.Seat;
import com.project.cinemabackend.model.TicketStatus;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper()
public interface SeatMapper {
    @Mapping(target = "rowNumber", source = "rowNumber")
    SeatDTO toSeatDto(Seat seat);


    @Mapping(target = "rowNumber", source = "rowNumber")
    @Mapping(
            target = "isAvailableForShowtime",
            expression = "java(bookingSeats.stream().noneMatch(bs -> " +
                    "bs.getSeat().getId().equals(seat.getId()) && " +
                    "java.util.Objects.equals(bs.getStatus(), com.project.cinemabackend.model.TicketStatus.VALID)" +
                    "))"
    )
    SeatShowtimeDTO toSeatShowtimeDto(Seat seat, @Context List<BookingSeat> bookingSeats);
    List<SeatShowtimeDTO> toSeatShowtimeDtoList(List<Seat> seats, @Context List<BookingSeat> bookingSeats);

}
