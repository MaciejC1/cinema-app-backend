package com.project.cinemabackend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    private final Key key;
    private final long accessExpSec;
    private final long refreshExpSec;
    private final String issuer;
    private final String audience;
    private static final int MAX_TOKEN_LENGTH = 8192;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.accessTokenExpirationSec}") long accessExpSec,
            @Value("${app.security.jwt.refreshTokenExpirationSec}") long refreshExpSec,
            @Value("${app.security.jwt.issuer:myapp}") String issuer,
            @Value("${app.security.jwt.audience:myapp-users}") String audience
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpSec = accessExpSec;
        this.refreshExpSec = refreshExpSec;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String generateAccessToken(UUID userId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExpSec)))
                .claim("roles", roles)
                .claim("typ", "access")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExpSec)))
                .claim("typ", "refresh")
                .claim("createdAt", now.toString())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) throws JwtException {
        if (token == null || token.length() > MAX_TOKEN_LENGTH) {
            throw new JwtException("Token length exceeds maximum allowed size");
        }

        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseClaimsJws(token);

            log.debug("Successfully parsed JWT token for user: {}", jws.getBody().getSubject());
            return jws;
        } catch (ExpiredJwtException e) {
            log.debug("Token expired for user: {}", e.getClaims().getSubject());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Illegal JWT argument: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isTokenValid(String token, String expectedType) {
        try {
            Jws<Claims> jws = parseToken(token);
            String typ = jws.getBody().get("typ", String.class);
            boolean valid = expectedType.equals(typ) && jws.getBody().getExpiration().after(new Date());

            if (!valid) {
                log.debug("Token validation failed - expected type: {}, actual: {}", expectedType, typ);
            }

            return valid;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}