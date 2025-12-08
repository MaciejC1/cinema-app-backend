package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.ShowtimeDTO;
import com.project.cinemabackend.model.Showtime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper()
public interface ShowtimeMapper {
    @Mapping(target = "hallName", expression = "java(showtime.getHall().getName())")
    @Mapping(target = "startTime", expression = "java(showtime.getStartTime().toLocalDateTime())")
    ShowtimeDTO toDto(Showtime showtime);
    List<ShowtimeDTO> toDtoList(List<Showtime> showtimes);
}
