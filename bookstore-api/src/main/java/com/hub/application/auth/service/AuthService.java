package com.hub.application.auth.service;


import com.hub.application.auth.port.in.LoginUseCase;
import com.hub.application.auth.port.in.LogoutUseCase;
import com.hub.application.auth.port.in.command.LoginCommand;
import com.hub.application.auth.port.in.command.TokenGenerationCommand;
import com.hub.application.auth.port.in.result.LoginResult;
import com.hub.application.auth.port.out.PasswordHasherPort;
import com.hub.application.auth.port.out.TokenGeneratorPort;
import com.hub.application.auth.port.out.TokenMetadataRepositoryPort;
import com.hub.application.identity.port.out.UserRepositoryPort;
import com.hub.domain.auth.TokenMetadata;
import com.hub.domain.auth.TokenStatus;
import com.hub.domain.auth.exception.InactiveUserException;
import com.hub.domain.auth.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class AuthService implements LoginUseCase, LogoutUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenMetadataRepositoryPort tokenMetadataPort;
    private final PasswordHasherPort passwordHasher;
    private final TokenGeneratorPort tokenGenerator;
    private final long jwtExpirationHours;

    @Override
    public LoginResult login(LoginCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        var user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new InactiveUserException("User account is not active");
        }

        if (!passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String tokenId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofHours(jwtExpirationHours));

        tokenMetadataPort.save(TokenMetadata.builder()
                .tokenId(tokenId)
                .userId(user.getId())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .status(TokenStatus.ACTIVE)
                .build());

        String jwt = tokenGenerator.generate(new TokenGenerationCommand(
                user.getId(), user.getUsername(), tokenId, user.getRoles(), expiresAt));

        return new LoginResult(jwt, user.getId(), user.getRoles());
    }

    @Override
    public void logout(String tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        tokenMetadataPort.revokeToken(tokenId);
    }
}
