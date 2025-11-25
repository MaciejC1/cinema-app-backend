package com.project.cinemabackend.controller;

import com.project.cinemabackend.dto.UserDTO;
import com.project.cinemabackend.security.UserPrincipal;
import com.project.cinemabackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {
    UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
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
}
