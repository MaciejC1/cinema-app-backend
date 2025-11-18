package com.project.cinemabackend.repository;


import com.project.cinemabackend.model.Movie;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface MovieRepository extends CrudRepository<Movie, UUID> {

}
