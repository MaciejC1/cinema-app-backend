package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.model.UserRating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<UserRating, UUID> {
    @EntityGraph(attributePaths = {"movie", "movie.movieGenres", "movie.movieTags"})
    List<UserRating> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
