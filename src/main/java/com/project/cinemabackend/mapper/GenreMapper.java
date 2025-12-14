package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.GenreDTO;
import com.project.cinemabackend.model.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper()
public interface GenreMapper {
    GenreDTO toGenreDto (Genre genre);
    List<GenreDTO> toGenreDtoList (List<Genre> genres);
}
