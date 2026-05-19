package com.hub.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** Session token metadata persisted in Redis. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenMetadata {

    private String tokenId;
    private UUID userId;
    private Instant issuedAt;
    private Instant expiresAt;
    private TokenStatus status;

    public boolean isActive() {
        return TokenStatus.ACTIVE.equals(this.status);
    }
}