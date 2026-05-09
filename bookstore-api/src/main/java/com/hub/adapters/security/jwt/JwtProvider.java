package com.hub.adapters.security.jwt;

import com.hub.application.auth.port.in.command.TokenGenerationCommand;
import com.hub.application.auth.port.out.TokenGeneratorPort;
import com.hub.domain.identity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/** Implements JWT generation (TokenGeneratorPort) and exposes parsing for JwtAuthFilter. */
@Slf4j
@Component
public class JwtProvider implements TokenGeneratorPort {

    @Value("${app.jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    public String generate(TokenGenerationCommand command) {
        List<String> roleNames = command.roles().stream()
                .map(Role::name).collect(Collectors.toList());

        return Jwts.builder()
                .subject(command.username())
                .id(command.tokenId())
                .claim("userId", command.userId())
                .claim("roles", roleNames)
                .issuedAt(new Date())
                .expiration(Date.from(command.expiresAt()))
                .signWith(secretKey)
                .compact();
    }

    /** Parses and validates JWT, returning claims. Throws JwtException on failure. */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }
}
