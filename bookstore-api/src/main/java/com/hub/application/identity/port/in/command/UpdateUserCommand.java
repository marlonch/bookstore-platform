package com.hub.application.identity.port.in.command;

import com.hub.domain.identity.Role;
import com.hub.domain.identity.UserId;
import com.hub.domain.identity.UserStatus;

import java.util.Set;

public record UpdateUserCommand(
        UserId id,
        String username,
        String email,
        String rawPassword,
        Set<Role> roles,
        UserStatus status
) {}
