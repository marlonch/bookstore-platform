package com.hub.domain.auth;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TokenMetadataTest {

    @Test
    void isActive_whenStatusIsActive_returnsTrue() {
        TokenMetadata metadata = TokenMetadata.builder()
                .tokenId("token-123")
                .userId(UUID.randomUUID())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .status(TokenStatus.ACTIVE)
                .build();

        assertThat(metadata.isActive()).isTrue();
    }

    @Test
    void isActive_whenStatusIsRevoked_returnsFalse() {
        TokenMetadata metadata = TokenMetadata.builder()
                .tokenId("token-123")
                .userId(UUID.randomUUID())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .status(TokenStatus.REVOKED)
                .build();

        assertThat(metadata.isActive()).isFalse();
    }
}