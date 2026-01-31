package com.project.cinemabackend.dto;

import java.util.List;
import java.util.UUID;

public record UserDTO(
        UUID userId,
        String lastName,
        String name,
        String email,
        String phone,
        List<String> roles
) {}
