package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.Hall;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface HallRepository extends CrudRepository<Hall, UUID> {
}
