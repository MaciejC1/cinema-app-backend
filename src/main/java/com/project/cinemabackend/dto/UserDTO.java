package com.project.cinemabackend.dto;

import java.util.List;
import java.util.UUID;

public record UserDTO(
        UUID userId,
        String email,
        List<String> roles
) {}
