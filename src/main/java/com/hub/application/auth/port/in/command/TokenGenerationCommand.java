package com.hub.application.auth.port.in.command;

import com.hub.domain.identity.Role;

import java.time.Instant;
import java.util.Set;

/**
 * Command carrying the data required to generate an authentication token
 * for a user session.
 *
 * @param userId    identifier of the authenticated user
 * @param username  username associated with the token
 * @param tokenId   unique identifier of the token
 * @param roles     roles granted to the user
 * @param expiresAt token expiration instant
 */
public record TokenGenerationCommand(
        Long userId,
        String username,
        String tokenId,
        Set<Role> roles,
        Instant expiresAt
) {}
