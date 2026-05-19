package com.hub.domain.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/** Core domain user — no Spring dependency. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private UserId id;
    private String username;
    private String email;
    private String passwordHash;

    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    private UserStatus status;

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    public boolean isAdministrator() {
        return this.roles.contains(Role.ADMINISTRATOR);
    }
}