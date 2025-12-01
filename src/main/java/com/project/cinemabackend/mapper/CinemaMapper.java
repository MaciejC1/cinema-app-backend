package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.CinemaMinimalDTO;
import com.project.cinemabackend.model.Cinema;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper()
public interface CinemaMapper {
    CinemaMinimalDTO toMinimalDto(Cinema cinema);
    List<CinemaMinimalDTO> toMinimalDtoList(List<Cinema> cinemas);
}
