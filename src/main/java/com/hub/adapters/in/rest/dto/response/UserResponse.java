package com.hub.adapters.in.rest.dto.response;


import com.hub.domain.identity.Role;
import com.hub.domain.identity.UserStatus;

import java.util.Set;

public record UserResponse(Long id, String username, String email, Set<Role> roles, UserStatus status) {}
