package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.UserRole;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserRoleRepository extends CrudRepository<UserRole, UUID> {
}
