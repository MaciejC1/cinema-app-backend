package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.Booking;
import com.project.cinemabackend.model.Showtime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShowtimeRepository extends JpaRepository<Showtime, UUID> {
    @EntityGraph(attributePaths = {
            "movie.media",
            "hall.cinema",
            "hall.seats"
    })
    Optional<Showtime> findShowtimeById(UUID id);
}
