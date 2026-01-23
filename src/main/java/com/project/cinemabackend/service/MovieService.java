package com.project.cinemabackend.service;

import com.project.cinemabackend.dto.*;
import com.project.cinemabackend.mapper.MovieMapper;
import com.project.cinemabackend.mapper.ShowtimeMapper;
import com.project.cinemabackend.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final ShowtimeMapper showtimeMapper;

    MovieService(MovieRepository movieRepository, MovieMapper  movieMapper, ShowtimeMapper showtimeMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.showtimeMapper = showtimeMapper;
    }

    public List<MovieDTO> findMoviesIsActive() {
        return movieMapper.toDtoList(movieRepository.findByIsActiveTrue());
    }

    public MovieDetailsDTO findMovieById(UUID id) {
        return movieMapper.toDetailsDto(movieRepository.findMovieById(id));
    }

    public MovieDetailsDTO findMovieBySlug(String slug) {
        return movieMapper.toDetailsDto(movieRepository.findMovieBySlug(slug));
    }


    public List<MovieMinimalDTO> findMinMoviesIsUpcoming() {
        return movieMapper.toMinimalDtoList(movieRepository.findByIsUpcomingTrue());
    }

    public List<MovieShowtimesDTO> getMoviesForDateAndGenres(LocalDate date, String genre, UUID cinemaId) {
        OffsetDateTime dateConverted = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateConverted = dateConverted.plusDays(1);
        return movieMapper.toMoviesShowtimesListDto(movieRepository.findMoviesByDateAndGenre(dateConverted,endDateConverted, genre, cinemaId));
    }

    public MovieShowtimesDTO getMovieForSlug(String slug, UUID cinemaId) {
        return movieMapper.toMoviesShowtimesDto(movieRepository.findMovieWithShowtimesBySlug(slug, cinemaId));
    }

    public List<MovieSurveyDTO> getMoviesIsRecommended() {
        return movieMapper.toMovieSurveyDtoList(movieRepository.findAllByIsRecommendedIsTrue());
    }
}
