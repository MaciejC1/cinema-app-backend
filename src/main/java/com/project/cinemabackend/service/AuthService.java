package com.project.cinemabackend.service;


import com.project.cinemabackend.dto.auth.LoginRequest;
import com.project.cinemabackend.dto.auth.LoginResponse;
import com.project.cinemabackend.dto.auth.RegisterRequest;
import com.project.cinemabackend.dto.auth.RegisterResponse;
import com.project.cinemabackend.model.ExpiredRefreshToken;
import com.project.cinemabackend.model.Role;
import com.project.cinemabackend.model.User;
import com.project.cinemabackend.model.UserRole;
import com.project.cinemabackend.repository.ExpiredRefreshTokenRepository;
import com.project.cinemabackend.repository.RoleRepository;
import com.project.cinemabackend.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExpiredRefreshTokenRepository expiredTokenRepository;
    private final boolean secureCookie;

    private static final long TOKEN_GRACE_PERIOD_SEC = 30;

    public AuthService(AuthenticationManager authManager, JwtService jwtService,
                       UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, ExpiredRefreshTokenRepository expiredTokenRepository,
                       @Value("${app.security.cookie.secure:false}") boolean secureCookie) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.expiredTokenRepository = expiredTokenRepository;
        this.secureCookie = secureCookie;
    }

    @Transactional
    public ResponseEntity<LoginResponse> login(LoginRequest request, HttpServletResponse response) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            User user = userRepository.findByEmailWithRoles(request.email())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            List<String> roleNamesStr = user.getUserRoles().stream()
                    .map(role -> role.getRole().getRoleName())
                    .toList();

            String accessToken = jwtService.generateAccessToken(user.getId(), roleNamesStr);
            String refreshToken = jwtService.generateRefreshToken(user.getId());

            setAuthCookies(response, accessToken, refreshToken);

            log.info("User logged in successfully: {}", user.getEmail());

            return ResponseEntity.ok(new LoginResponse("Zalogowano"));

        } catch (AuthenticationException ex) {
            log.warn("Failed login attempt for email: {}", request.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Nieprawidłowy login lub hasło"));
        }
    }

    @Transactional
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "REFRESH_TOKEN");

        if (refreshToken == null) {
            log.debug("Refresh attempt without token");
            clearCookies(response);
            return ResponseEntity.status(401).body(new LoginResponse("Brak refresh token"));
        }

        ExpiredRefreshToken expiredToken = expiredTokenRepository.findByRefreshToken(refreshToken)
                .orElse(null);

        if (expiredToken != null) {
            OffsetDateTime gracePeriodEnd = expiredToken.getInvalidatedAt().plusSeconds(TOKEN_GRACE_PERIOD_SEC);
            if (OffsetDateTime.now(ZoneOffset.UTC).isAfter(gracePeriodEnd)) {
                log.warn("Attempt to use invalidated refresh token outside grace period");
                clearCookies(response);
                return ResponseEntity.status(401).body(new LoginResponse("Token został unieważniony"));
            }
            log.debug("Token used within grace period");
        }

        if (!jwtService.isTokenValid(refreshToken, "refresh")) {
            log.warn("Invalid refresh token received");
            clearCookies(response);
            return ResponseEntity.status(401).body(new LoginResponse("Nieprawidłowy refresh token"));
        }

        var jws = jwtService.parseToken(refreshToken);
        UUID userId = UUID.fromString(jws.getBody().getSubject());
        String createdAtStr = jws.getBody().get("createdAt", String.class);
        Instant createdAt = Instant.parse(createdAtStr);

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> {
                    log.error("User not found for ID: {}", userId);
                    clearCookies(response);
                    return new UsernameNotFoundException("Użytkownik nie znaleziony");
                });

        if (user.getUpdatedAt() != null && createdAt.isBefore(user.getUpdatedAt().toInstant())) {
            invalidateRefreshToken(refreshToken, user, "password_changed");
            clearCookies(response);
            log.info("Token invalidated due to password change for user: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Token nieważny - hasło zostało zmienione"));
        }

        invalidateRefreshToken(refreshToken, user, "rotated");

        List<String> roleNamesStr = user.getUserRoles().stream()
                .map(role -> role.getRole().getRoleName())
                .toList();

        String newAccessToken = jwtService.generateAccessToken(user.getId(), roleNamesStr);
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        setAuthCookies(response, newAccessToken, newRefreshToken);

        log.debug("Tokens refreshed successfully for user: {}", user.getEmail());

        return ResponseEntity.ok(new LoginResponse("Token odświeżony"));
    }

    @Transactional
    public ResponseEntity<LoginResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "REFRESH_TOKEN");

        if (refreshToken != null && jwtService.isTokenValid(refreshToken, "refresh")) {
            var jws = jwtService.parseToken(refreshToken);
            UUID userId = UUID.fromString(jws.getBody().getSubject());


            userRepository.findById(userId).ifPresent(user -> {
                invalidateRefreshToken(refreshToken, user, "logout");
                log.info("User logged out: {}", user.getEmail());
            });
        }

        clearCookies(response);
        return ResponseEntity.ok(new LoginResponse("Wylogowano"));
    }

    @Transactional
    public ResponseEntity<RegisterResponse> register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Registration attempt with existing email: {}", request.email());
            return ResponseEntity.badRequest()
                    .body(new RegisterResponse("Użytkownik o tym e-mailu już istnieje"));
        }

        Role role = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono roli"));

        String hashed = passwordEncoder.encode(request.password());

        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setPasswordHash(hashed);
        //newUser.setCreatedAt(Instant.now());

        UserRole userRole = new UserRole();
        userRole.setUser(newUser);
        userRole.setRole(role);

        newUser.getUserRoles().add(userRole);

        userRepository.save(newUser);

        log.info("New user registered: {}", newUser.getEmail());

        return ResponseEntity.ok(new RegisterResponse("Zarejestrowano pomyślnie"));
    }

    private void invalidateRefreshToken(String token, User user, String reason) {
        try {
            var jws = jwtService.parseToken(token);
            OffsetDateTime expiredAt = jws.getBody().getExpiration()
                    .toInstant()
                    .atOffset(ZoneOffset.UTC);
            OffsetDateTime issuedAt = jws.getBody().getIssuedAt()
                    .toInstant()
                    .atOffset(ZoneOffset.UTC);

            ExpiredRefreshToken expiredToken = ExpiredRefreshToken.builder()
                    .user(user)
                    .refreshToken(token)
                    .issuedAt(issuedAt)
                    .expiredAt(expiredAt)
                    .invalidatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .reason(reason)
                    .build();

            expiredTokenRepository.save(expiredToken);
            log.debug("Refresh token invalidated - reason: {}, user: {}", reason, user.getEmail());
        } catch (Exception e) {
            log.error("Error while invalidating refresh token for user {}: {}",
                    user.getEmail(), e.getMessage(), e);
        }
    }

    private static String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/public/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private void clearCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/public/auth")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}