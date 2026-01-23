package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.LastBookingDTO;
import com.project.cinemabackend.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(uses = {ShowtimeMapper.class, MovieMapper.class, CinemaMapper.class, BookingSeatMapper.class, SeatMapper.class })
public interface BookingMapper {
    @Mapping(target = "cinema", source = "showtime.hall.cinema")
    @Mapping(target = "movie", source = "showtime.movie")
    @Mapping(target = "amount", source = "totalAmount")
    LastBookingDTO toLastBookingDto(Booking booking);
}

