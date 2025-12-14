package com.project.cinemabackend.repository;


import com.project.cinemabackend.model.Tag;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TagRepository extends CrudRepository<Tag, UUID> {
}