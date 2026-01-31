package com.project.cinemabackend.service;

import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.model.User;
import com.project.cinemabackend.model.UserRating;
import com.project.cinemabackend.repository.MovieRepository;
import com.project.cinemabackend.repository.UserRatingRepository;
import com.project.cinemabackend.repository.UserRepository;
import com.project.cinemabackend.security.UserPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Service
public class RatingService {
    @PersistenceContext
    private EntityManager em;
    UserRepository userRepository;
    UserRatingRepository userRatingRepository;
    MovieRepository movieRepository;


    RatingService(UserRepository userRepository, UserRatingRepository userRatingRepository, MovieRepository movieRepository) {
        this.userRepository = userRepository;
        this.userRatingRepository = userRatingRepository;
        this.movieRepository = movieRepository;
    }

    public void addOrUpdateRating(UUID movieId, Authentication authentication, int rating) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not authenticated"
            );
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = principal.getUserId();

        Optional<Movie> optionalMovie = movieRepository.findById(movieId);
        Movie  movie = optionalMovie.orElseThrow();
        User user = em.getReference(User.class, userId);

        Optional<UserRating> existingRating = userRatingRepository.findByMovie_IdAndUser_Id(movieId, userId);
        if(existingRating.isEmpty()) {
            movie.setRatingCount(movie.getRatingCount() + 1);
        }

        if(existingRating.isPresent()) {
            UserRating userRating = existingRating.get();
            BigDecimal newValue = movie.getAverageRating().subtract((BigDecimal.valueOf(userRating.getRating())));
            movie.setAverageRating(newValue);
        }

        UserRating userRating = existingRating.orElseGet(UserRating::new);


        userRating.setUser(user);
        userRating.setMovie(movie);
        userRating.setRating(rating);

        userRatingRepository.save(userRating);
        BigDecimal sum = movie.getAverageRating().add((BigDecimal.valueOf(rating)));
        BigDecimal newAverage = sum.divide(BigDecimal.valueOf(movie.getRatingCount()), RoundingMode.HALF_UP);
        movie.setAverageRating(newAverage);

        movieRepository.save(movie);
    }
}
