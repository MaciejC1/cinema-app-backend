package com.project.cinemabackend.repository;


import com.project.cinemabackend.model.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {
    @EntityGraph(attributePaths = {
            "movieDirectors.director",
            "movieGenres.genre",
            "media"
    })
    List<Movie> findByIsActiveTrue();

    @EntityGraph(attributePaths = {
            "movieDirectors.director",
            "movieGenres.genre",
            "media"
    })
    List<Movie> findByIsUpcomingTrue();

    Movie findMovieById(UUID id);

    Movie findMovieBySlug(String slug);

    @Query("""
    SELECT DISTINCT m
    FROM Movie m
    LEFT JOIN FETCH m.movieGenres g
    LEFT JOIN FETCH m.showtimes s
    LEFT JOIN s.hall h
    WHERE s.startTime >= :date
      AND s.startTime < :endDate
      AND h.cinema.id = :cinemaId
      AND (:genreName IS NULL OR g.genre.name = :genreName)
""")
    List<Movie> findMoviesByDateAndGenre(
            @Param("date") OffsetDateTime date,
            @Param("endDate") OffsetDateTime endDate,
            @Param("genreName") String genreName,
            @Param("cinemaId") UUID cinemaId
    );

    @Query("""
    SELECT DISTINCT m
    FROM Movie m
    LEFT JOIN FETCH m.showtimes s
    LEFT JOIN FETCH s.hall h
    LEFT JOIN FETCH m.movieGenres mg
    LEFT JOIN FETCH mg.genre g
    WHERE m.slug = :slug
      AND m.isActive = TRUE
      AND s.isActive = TRUE
      AND h.cinema.id = :cinemaId
    ORDER BY s.startTime ASC
""")
    Movie findMovieWithShowtimesBySlug(
            @Param("slug") String slug,
            @Param("cinemaId") UUID cinemaId
    );

    List<Movie> findAllByIsRecommendedIsTrue();

    List<Movie> findAllByIsActiveTrue();
}
