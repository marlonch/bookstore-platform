package com.hub.adapters.in.rest.mapper;

import com.hub.adapters.in.rest.dto.request.CreateUserRequest;
import com.hub.adapters.in.rest.dto.request.UpdateUserRequest;
import com.hub.adapters.in.rest.dto.response.UserResponse;
import com.hub.application.identity.port.in.command.CreateUserCommand;
import com.hub.application.identity.port.in.command.UpdateUserCommand;
import com.hub.domain.identity.Role;
import com.hub.domain.identity.User;
import com.hub.domain.identity.UserId;
import com.hub.domain.identity.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRestMapperTest {

    private final UserRestMapper mapper = new UserRestMapper();
    private static final UUID USER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UserId USER_ID = new UserId(USER_UUID);
    
    @Test
    void toResponse_mapsAllUserFields() {
        User user = User.builder()
                .id(USER_ID)
                .username("alice")
                .email("alice@example.com")
                .roles(Set.of(Role.ADMINISTRATOR))
                .status(UserStatus.ACTIVE)
                .build();

        UserResponse response = mapper.toResponse(user);

        assertThat(response.id()).isEqualTo(USER_UUID);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.roles()).containsExactly(Role.ADMINISTRATOR);
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
    }
    
    @Test
    void toCreateCommand_withRoles_usesProvidedRoles() {
        CreateUserRequest request = new CreateUserRequest("alice", "alice@example.com", "Password1!", Set.of(Role.ADMINISTRATOR));

        CreateUserCommand command = mapper.toCreateCommand(request);

        assertThat(command.roles()).containsExactly(Role.ADMINISTRATOR);
    }

    @Test
    void toCreateCommand_withNullRoles_defaultsToNonAdministrator() {
        CreateUserRequest request = new CreateUserRequest("alice", "alice@example.com", "Password1!", null);

        CreateUserCommand command = mapper.toCreateCommand(request);

        assertThat(command.roles()).containsExactly(Role.NON_ADMINISTRATOR);
    }

    @Test
    void toCreateCommand_withEmptyRoles_defaultsToNonAdministrator() {
        CreateUserRequest request = new CreateUserRequest("alice", "alice@example.com", "Password1!", Set.of());

        CreateUserCommand command = mapper.toCreateCommand(request);

        assertThat(command.roles()).containsExactly(Role.NON_ADMINISTRATOR);
    }
    
    @Test
    void toUpdateCommand_mapsAllFields() {
        UpdateUserRequest request = new UpdateUserRequest("bob", "bob@example.com", "NewPass1!", Set.of(Role.NON_ADMINISTRATOR), UserStatus.INACTIVE);

        UpdateUserCommand command = mapper.toUpdateCommand(USER_UUID, request);

        assertThat(command.id()).isEqualTo(USER_ID);
        assertThat(command.username()).isEqualTo("bob");
        assertThat(command.email()).isEqualTo("bob@example.com");
        assertThat(command.rawPassword()).isEqualTo("NewPass1!");
        assertThat(command.roles()).containsExactly(Role.NON_ADMINISTRATOR);
        assertThat(command.status()).isEqualTo(UserStatus.INACTIVE);
    }
}