package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.model.UserRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRatingRepository extends JpaRepository<UserRating, UUID> {
    List<UserRating> findTop15ByUserIdOrderByCreatedAtDesc(UUID userId);
    List<UserRating> findAllByMovie_Id(UUID movieId);
}
