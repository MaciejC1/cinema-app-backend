package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends CrudRepository<Role, UUID> {
    Optional<Role> findByRoleName(String roleName);
}
