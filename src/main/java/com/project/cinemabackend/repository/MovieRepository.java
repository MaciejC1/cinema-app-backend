package com.project.cinemabackend.repository;


import com.project.cinemabackend.model.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {
//    @Query("""
//    select distinct m from Movie m
//    left join fetch m.movieDirectors md
//    left join fetch md.director d
//    left join fetch m.movieGenres mg
//    left join fetch mg.genre g
//    left join fetch m.media med
//    where m.isActive = true
//""")
//    List<Movie> findAllFullMoviesByIsActiveIsTrue();

    @EntityGraph(attributePaths = {
            "movieDirectors.director",
            "movieGenres.genre",
            "media"
    })
    List<Movie> findByIsActiveTrue();

    Movie findMovieById(UUID id);
}
