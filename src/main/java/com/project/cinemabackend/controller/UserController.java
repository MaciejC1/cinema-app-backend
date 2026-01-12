package com.project.cinemabackend.controller;

import com.project.cinemabackend.dto.GenrePreferenceDto;
import com.project.cinemabackend.dto.PreferenceStatusResponse;
import com.project.cinemabackend.dto.UserDTO;
import com.project.cinemabackend.security.UserPrincipal;
import com.project.cinemabackend.service.UserEmbeddingService;
import com.project.cinemabackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {
    UserService userService;
    UserEmbeddingService userEmbeddingService;

    UserController(UserService userService, UserEmbeddingService userEmbeddingService) {
        this.userService = userService;
        this.userEmbeddingService = userEmbeddingService;
    }

    @GetMapping("/auth/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated request to /me endpoint");
            return ResponseEntity.status(401).build();
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = principal.getUserId();

        log.debug("Fetching user details for userId: {}", userId);
        UserDTO userDTO = userService.getCurrentUser(userId);

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/preferences/status")
    public ResponseEntity<PreferenceStatusResponse> getPreferenceStatus(
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = principal.getUserId();

        boolean hasPreferences = userEmbeddingService.hasPreferences(userId);

        return ResponseEntity.ok(
                new PreferenceStatusResponse(hasPreferences)
        );
    }

    @GetMapping("/preferences/genres")
    public ResponseEntity<List<GenrePreferenceDto>> getUserGenrePreference (Authentication authentication)
    {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = principal.getUserId();

        return ResponseEntity.ok(userService.getFavoriteGenresForUser(userId));
    }

}
