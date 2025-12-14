package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.Genre;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenreRepository extends CrudRepository<Genre, UUID>  {
    List<Genre> findAllByOrderByNameAsc();
}

