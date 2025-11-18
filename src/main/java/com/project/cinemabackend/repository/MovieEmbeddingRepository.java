package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.MovieEmbedding;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface MovieEmbeddingRepository extends CrudRepository<MovieEmbedding, UUID> {
    Optional<MovieEmbedding> findByMovie_Id(UUID movieId);

    Optional<MovieEmbedding> findFirstByOrderByCreatedAtDesc();
}

