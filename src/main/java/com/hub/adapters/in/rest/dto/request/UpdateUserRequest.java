package com.hub.adapters.in.rest.dto.request;

import com.hub.domain.identity.Role;
import com.hub.domain.identity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateUserRequest(
        @Size(min = 3, max = 100) String username,
        @Email String email,
        @Size(min = 8) String password,
        Set<Role> roles,
        UserStatus status
) {}
