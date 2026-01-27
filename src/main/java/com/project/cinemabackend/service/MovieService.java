package com.project.cinemabackend.service;

import com.project.cinemabackend.dto.*;
import com.project.cinemabackend.mapper.MovieMapper;
import com.project.cinemabackend.mapper.RatingMapper;
import com.project.cinemabackend.mapper.ShowtimeMapper;
import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.model.User;
import com.project.cinemabackend.model.UserRating;
import com.project.cinemabackend.repository.MovieRepository;
import com.project.cinemabackend.repository.UserRepository;
import com.project.cinemabackend.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    private final UserRepository userRepository;
    private final RatingMapper ratingMapper;

    MovieService(MovieRepository movieRepository, MovieMapper  movieMapper, ShowtimeMapper showtimeMapper, UserRepository userRepository,  RatingMapper ratingMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.showtimeMapper = showtimeMapper;
        this.userRepository = userRepository;
        this.ratingMapper = ratingMapper;
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

    public List<RatingMovieDTO> getMoviesWithRatingByUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not authenticated"
            );
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = principal.getUserId();
        User user = userRepository.findUserWithMoviesById(userId);

        return user.getBookings().stream()
                .map(b -> b.getShowtime().getMovie())
                .distinct()
                .map(movie -> {
                    Integer rating = movie.getUserRatings().stream()
                            .filter(r -> r.getUser().getId().equals(userId))
                            .map(UserRating::getRating)
                            .findFirst()
                            .orElse(0);

                    RatingMovieDTO dto = new RatingMovieDTO();
                    dto.setMovieMinimalDTO(movieMapper.toMinimalDto(movie));
                    dto.setRating(rating);

                    return dto;
                })
                .toList();

    }
}
