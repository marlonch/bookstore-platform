package com.hub.application.auth.port.out;


import com.hub.domain.auth.TokenMetadata;

import java.util.Optional;

/**
 * Outbound port for storing and managing authentication token metadata.
 * <p>
 * This contract abstracts the persistence mechanism used to track issued
 * tokens, their lifecycle, and revocation state.
 */
public interface TokenMetadataRepositoryPort {

    /**
     * Persists the given token metadata.
     *
     * @param tokenMetadata token metadata to persist
     */
    void save(TokenMetadata tokenMetadata);

    /**
     * Finds token metadata by token identifier.
     *
     * @param tokenId token identifier
     * @return matching token metadata if it exists, otherwise an empty result
     */
    Optional<TokenMetadata> findByTokenId(String tokenId);

    /**
     * Revokes the token associated with the given identifier.
     *
     * @param tokenId token identifier
     */
    void revokeToken(String tokenId);

    /**
     * Revokes all active tokens associated with the given user.
     *
     * @param userId user identifier
     */
    void revokeAllUserTokens(Long userId);
}
