package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.MovieGenre;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface MovieGenreRepository extends CrudRepository<MovieGenre, UUID> {
}
