package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.model.UserRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRatingRepository extends JpaRepository<UserRating, UUID> {
    List<UserRating> findTop15ByUserIdOrderByCreatedAtDesc(UUID userId);
    List<UserRating> findAllByMovie_Id(UUID movieId);
    Optional<UserRating> findUserRatingByMovie_IdAndUser_Id(UUID movieId, UUID userId);
    Optional<UserRating> findUserRatingByMovieIdAndUserId(UUID movieId, UUID userId);
    Optional<UserRating> findByUser_IdAndMovie_Id(UUID userId, UUID movieId);
}
