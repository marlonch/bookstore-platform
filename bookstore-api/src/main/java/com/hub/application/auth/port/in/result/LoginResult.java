package com.hub.application.auth.port.in.result;

import com.hub.domain.identity.Role;

import java.util.Set;

public record LoginResult(String token, Long userId, Set<Role> roles) {}
