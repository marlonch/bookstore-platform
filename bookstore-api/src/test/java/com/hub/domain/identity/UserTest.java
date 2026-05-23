package com.hub.domain.identity;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private static final UserId USER_ID = new UserId(UUID.randomUUID());

    // --- isActive ---

    @Test
    void isActive_whenStatusIsActive_returnsTrue() {
        User user = User.builder()
                .id(USER_ID)
                .status(UserStatus.ACTIVE)
                .build();

        assertThat(user.isActive()).isTrue();
    }

    @Test
    void isActive_whenStatusIsInactive_returnsFalse() {
        User user = User.builder()
                .id(USER_ID)
                .status(UserStatus.INACTIVE)
                .build();

        assertThat(user.isActive()).isFalse();
    }

    @Test
    void isActive_whenStatusIsBanned_returnsFalse() {
        User user = User.builder()
                .id(USER_ID)
                .status(UserStatus.BANNED)
                .build();

        assertThat(user.isActive()).isFalse();
    }

    // --- isAdministrator ---

    @Test
    void isAdministrator_withAdminRole_returnsTrue() {
        User user = User.builder()
                .id(USER_ID)
                .roles(Set.of(Role.ADMINISTRATOR))
                .status(UserStatus.ACTIVE)
                .build();

        assertThat(user.isAdministrator()).isTrue();
    }

    @Test
    void isAdministrator_withNonAdminRole_returnsFalse() {
        User user = User.builder()
                .id(USER_ID)
                .roles(Set.of(Role.NON_ADMINISTRATOR))
                .status(UserStatus.ACTIVE)
                .build();

        assertThat(user.isAdministrator()).isFalse();
    }

    @Test
    void isAdministrator_withNoRoles_returnsFalse() {
        User user = User.builder()
                .id(USER_ID)
                .status(UserStatus.ACTIVE)
                .build();

        assertThat(user.isAdministrator()).isFalse();
    }
}