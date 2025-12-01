package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShowTimeRepository extends JpaRepository<Showtime, UUID> {

}
