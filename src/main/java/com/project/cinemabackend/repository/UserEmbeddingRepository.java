package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.UserEmbedding;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserEmbeddingRepository extends CrudRepository<UserEmbedding, UUID> {
    Optional<UserEmbedding> findByUser_Id(UUID userId);

    boolean existsByUser_Id(UUID userId);
}
