package com.hub.application.identity.port.in.command;

import com.hub.domain.identity.Role;

import java.util.Set;

public record CreateUserCommand(
        String username,
        String email,
        String rawPassword,
        Set<Role> roles
) {}
