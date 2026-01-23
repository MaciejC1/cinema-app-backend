package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.SeatShowtimeDTO;
import com.project.cinemabackend.dto.ShowtimeDTO;
import com.project.cinemabackend.dto.ShowtimeDetailsDTO;
import com.project.cinemabackend.model.BookingSeat;
import com.project.cinemabackend.model.Media;
import com.project.cinemabackend.model.Seat;
import com.project.cinemabackend.model.Showtime;
import com.project.cinemabackend.model.TicketStatus;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mapper(uses = {SeatMapper.class, CinemaMapper.class})
public interface ShowtimeMapper {
    @Mapping(target = "hallName", expression = "java(showtime.getHall().getName())")
    @Mapping(target = "screenType", expression = "java(showtime.getHall().getScreenType())")
    @Mapping(target = "startTime", expression = "java(showtime.getStartTime().toLocalDateTime())")
    @Mapping(target = "endTime", expression = "java(showtime.getEndTime().toLocalDateTime())")
    ShowtimeDTO toDto(Showtime showtime);
    List<ShowtimeDTO> toDtoList(List<Showtime> showtimes);

    @Mapping(target = "hallName", expression = "java(showtime.getHall().getName())")
    @Mapping(target = "screenType", expression = "java(showtime.getHall().getScreenType())")
    @Mapping(target = "startTime", expression = "java(showtime.getStartTime().toLocalDateTime())")
    @Mapping(target = "endTime", expression = "java(showtime.getEndTime().toLocalDateTime())")
    @Mapping(target = "movie.poster", expression = "java(mapPoster(movie.getMedia()))")
    @Mapping(target = "seats", expression = "java(seatMapper.toSeatShowtimeDtoList(showtime.getHall().getSeats().stream().toList(), bookingSeatIds))")
    @Mapping(target = "cinema", source = "showtime.hall.cinema")
    ShowtimeDetailsDTO toDetailsDto(Showtime showtime, @Context List<BookingSeat> bookingSeatIds, @Context SeatMapper seatMapper);


    default String mapPoster(Set<Media> media) {
        return media.stream()
                .filter(m -> "poster".equals(m.getMediaType()))
                .map(Media::getUrl)
                .findFirst()
                .orElse("");
    }

}
