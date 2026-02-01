package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.MovieTag;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface MovieTagRepository extends CrudRepository<MovieTag, UUID> {
}
