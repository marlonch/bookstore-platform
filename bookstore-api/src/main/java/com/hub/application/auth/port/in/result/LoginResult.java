package com.hub.application.auth.port.in.result;

import com.hub.domain.identity.Role;

import java.util.Set;
import java.util.UUID;

public record LoginResult(String token, UUID userId, Set<Role> roles) {}