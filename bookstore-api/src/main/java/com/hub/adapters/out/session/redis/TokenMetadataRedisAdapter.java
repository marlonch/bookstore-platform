package com.hub.adapters.out.session.redis;

import com.hub.adapters.out.session.redis.entity.TokenMetadataRedisEntity;
import com.hub.adapters.out.session.redis.repository.TokenMetadataRedisRepository;
import com.hub.application.auth.port.out.TokenMetadataRepositoryPort;
import com.hub.domain.auth.TokenMetadata;
import com.hub.domain.auth.TokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Redis-backed implementation of {@link TokenMetadataRepositoryPort}.
 *
 * <p>Stores token metadata with a Redis TTL derived from the token expiration
 * timestamp. Disabled in the {@code test} profile to avoid requiring a live
 * Redis instance during automated tests.</p>
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class TokenMetadataRedisAdapter implements TokenMetadataRepositoryPort {

    private final TokenMetadataRedisRepository redisRepository;

    @Override
    public void save(TokenMetadata tokenMetadata) {
        long ttl = Math.max(Duration.between(Instant.now(), tokenMetadata.getExpiresAt()).getSeconds(), 1L);
        redisRepository.save(TokenMetadataRedisEntity.builder()
                .tokenId(tokenMetadata.getTokenId())
                .userId(tokenMetadata.getUserId())
                .issuedAtEpoch(tokenMetadata.getIssuedAt().getEpochSecond())
                .expiresAtEpoch(tokenMetadata.getExpiresAt().getEpochSecond())
                .status(tokenMetadata.getStatus().name())
                .timeToLiveSeconds(ttl)
                .build());
    }

    @Override
    public Optional<TokenMetadata> findByTokenId(String tokenId) {
        return redisRepository.findById(tokenId).map(this::toDomain);
    }

    @Override
    public void revokeToken(String tokenId) {
        redisRepository.findById(tokenId).ifPresent(entity -> {
            entity.setStatus(TokenStatus.REVOKED.name());
            redisRepository.save(entity);
        });
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        redisRepository.findByUserId(userId).stream()
                .filter(e -> !TokenStatus.REVOKED.name().equals(e.getStatus()))
                .forEach(e -> {
                    e.setStatus(TokenStatus.REVOKED.name());
                    redisRepository.save(e);
                });
    }

    private TokenMetadata toDomain(TokenMetadataRedisEntity entity) {
        return TokenMetadata.builder()
                .tokenId(entity.getTokenId())
                .userId(entity.getUserId())
                .issuedAt(Instant.ofEpochSecond(entity.getIssuedAtEpoch()))
                .expiresAt(Instant.ofEpochSecond(entity.getExpiresAtEpoch()))
                .status(TokenStatus.valueOf(entity.getStatus()))
                .build();
    }
}
