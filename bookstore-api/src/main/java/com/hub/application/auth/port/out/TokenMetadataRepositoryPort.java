package com.hub.application.auth.port.out;

import com.hub.domain.auth.TokenMetadata;

import java.util.Optional;
import java.util.UUID;

public interface TokenMetadataRepositoryPort {

    void save(TokenMetadata tokenMetadata);

    Optional<TokenMetadata> findByTokenId(String tokenId);

    void revokeToken(String tokenId);

    void revokeAllUserTokens(UUID userId);
}
