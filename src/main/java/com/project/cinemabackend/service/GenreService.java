package com.project.cinemabackend.service;

import com.project.cinemabackend.dto.GenreDTO;
import com.project.cinemabackend.mapper.GenreMapper;
import com.project.cinemabackend.model.Genre;
import com.project.cinemabackend.repository.GenreRepository;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService {
    GenreRepository genreRepository;
    GenreMapper genreMapper;

    GenreService(GenreRepository genreRepository,  GenreMapper genreMapper) {
        this.genreRepository = genreRepository;
        this.genreMapper = genreMapper;
    }

    public List<GenreDTO> getAllGenres(){
        return genreMapper.toGenreDtoList(genreRepository.findAllByOrderByNameAsc());
    }
}
