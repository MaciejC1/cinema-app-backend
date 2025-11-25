package com.project.cinemabackend.service;

import com.project.cinemabackend.dto.MovieDTO;
import com.project.cinemabackend.dto.MovieDetailsDTO;
import com.project.cinemabackend.mapper.MovieMapper;
import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    MovieService(MovieRepository movieRepository, MovieMapper  movieMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
    }

    public List<MovieDTO> findMoviesIsActive() {
        return movieMapper.toDtoList(movieRepository.findByIsActiveTrue());
    }

    public MovieDetailsDTO findMovieById(String id) {
        UUID  uuid = UUID.fromString(id);
        System.out.println(movieRepository.findMovieById(uuid).getMovieGenres());
        return movieMapper.toDetailsDto(movieRepository.findMovieById(uuid));
    }
}
