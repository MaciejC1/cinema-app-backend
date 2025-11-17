package com.project.cinemabackend.util;

import com.project.cinemabackend.model.Role;
import com.project.cinemabackend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        createRoleIfNotExists("ROLE_USER", "Standard user role");
        createRoleIfNotExists("ROLE_ADMIN", "Administrator role");
    }

    private void createRoleIfNotExists(String roleName, String description) {
        roleRepository.findByRoleName(roleName).ifPresentOrElse(
                r -> {},
                () -> {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    role.setDescription(description);

                    roleRepository.save(role);
                }
        );
    }
}


