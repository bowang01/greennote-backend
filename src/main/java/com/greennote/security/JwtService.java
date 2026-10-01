package com.greennote.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecurityProperties properties;
    private SecretKey key;

    public JwtService(SecurityProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        byte[] secret = properties.getJwtSecret() == null
                ? new byte[0]
                : properties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("greennote.security.jwt-secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret);
    }

    public String createToken(Long userId, String username, String kind) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getExpireHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .claim("kind", kind)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public Instant expiresAt() {
        return Instant.now().plus(properties.getExpireHours(), ChronoUnit.HOURS);
    }

    public Optional<AuthPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Number userId = claims.get("uid", Number.class);
            String kind = claims.get("kind", String.class);
            if (userId == null || claims.getSubject() == null || kind == null) {
                return Optional.empty();
            }
            return Optional.of(new AuthPrincipal(userId.longValue(), claims.getSubject(), kind));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
