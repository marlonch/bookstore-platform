package com.hub.adapters.in.rest.dto.response;

import com.hub.domain.identity.Role;
import com.hub.domain.identity.UserStatus;

import java.util.Set;
import java.util.UUID;

public record UserResponse(UUID id, String username, String email, Set<Role> roles, UserStatus status) {}
