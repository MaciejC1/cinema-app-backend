package com.project.cinemabackend.dto.auth;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record RegisterRequest(
        @Email (message = "Niepoprawny adres e-mail")
        @NotBlank
        String email,

        @NotBlank
        @Size(max = 50, message = "Maksymalna liczba znaków dla imienia wynosi 50")
        String firstName,

        @NotBlank
        @Size(max = 50, message = "Maksymalna liczba znaków dla nazwiska wynosi 50")
        String lastName,

        @Nullable
        String phone,

        @NotNull
        @Past
        LocalDate dateOfBirth,

        @Nullable
        UUID preferredCinemaId,

        @NotBlank(message = "Hasło nie może być puste")
        @Size(min = 12, message = "Hasło musi mieć co najmniej 12 znaków")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Hasło musi zawierać małą i dużą literę, cyfrę oraz znak specjalny"
        )
        String passwordHash
) {}
