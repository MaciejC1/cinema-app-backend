package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.ExpiredRefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpiredRefreshTokenRepository extends CrudRepository<ExpiredRefreshToken, UUID> {
    Optional<ExpiredRefreshToken> findByRefreshToken(String refreshToken);
}
