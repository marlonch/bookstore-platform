package com.hub.app.service;

import com.hub.application.auth.port.out.PasswordHasherPort;
import com.hub.application.auth.port.out.TokenMetadataRepositoryPort;
import com.hub.application.identity.port.in.command.CreateUserCommand;
import com.hub.application.identity.port.in.command.UpdateUserCommand;
import com.hub.application.identity.port.out.UserRepositoryPort;
import com.hub.application.identity.service.UserService;
import com.hub.domain.auth.exception.UserNotFoundException;
import com.hub.domain.identity.Role;
import com.hub.domain.identity.User;
import com.hub.domain.identity.UserId;
import com.hub.domain.identity.UserStatus;
import com.hub.domain.identity.exception.DuplicateEmailException;
import com.hub.domain.identity.exception.DuplicateUsernameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final UserId USER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final UserId USER_ID_2 = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final UserId MISSING_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000099"));

    @Mock UserRepositoryPort userRepository;
    @Mock PasswordHasherPort passwordHasher;
    @Mock TokenMetadataRepositoryPort tokenMetadataPort;

    @InjectMocks
    UserService userService;

    @Test
    void createUser_withValidData_savesAndReturnsUser() {
        CreateUserCommand cmd = new CreateUserCommand("bob", "bob@test.com", "secret", Set.of(Role.NON_ADMINISTRATOR));

        when(userRepository.existsByEmail("bob@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(passwordHasher.encode("secret")).thenReturn("bcrypt-hash");

        User saved = User.builder().id(USER_ID).username("bob").email("bob@test.com")
                .passwordHash("bcrypt-hash").roles(Set.of(Role.NON_ADMINISTRATOR))
                .status(UserStatus.ACTIVE).build();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.createUser(cmd);

        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(argThat(u -> u.getPasswordHash().equals("bcrypt-hash")));
    }

    @Test
    void createUser_withDuplicateEmail_throwsDuplicateEmailException() {
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(
                new CreateUserCommand("bob", "dup@test.com", "secret", null)))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void createUser_withDuplicateUsername_throwsDuplicateUsernameException() {
        when(userRepository.existsByEmail("bob@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(
                new CreateUserCommand("bob", "bob@test.com", "secret", null)))
                .isInstanceOf(DuplicateUsernameException.class);
    }

    @Test
    void getUser_withExistingId_returnsUser() {
        User user = User.builder().id(USER_ID).username("alice").build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThat(userService.getUser(USER_ID).getUsername()).isEqualTo("alice");
    }

    @Test
    void getUser_withMissingId_throwsUserNotFoundException() {
        when(userRepository.findById(MISSING_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUser(MISSING_ID)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteUser_withExistingId_revokesTokensAndDeletes() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);

        userService.deleteUser(USER_ID);

        verify(tokenMetadataPort).revokeAllUserTokens(USER_ID.value());
        verify(userRepository).deleteById(USER_ID);
    }

    @Test
    void deleteUser_withMissingId_throwsUserNotFoundException() {
        when(userRepository.existsById(MISSING_ID)).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUser(MISSING_ID)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_disablingUser_revokesAllTokens() {
        User existing = User.builder().id(USER_ID).username("alice").email("a@test.com")
                .passwordHash("old-hash").roles(Set.of(Role.NON_ADMINISTRATOR))
                .status(UserStatus.ACTIVE).build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserCommand cmd = new UpdateUserCommand(USER_ID, null, null, null, null, UserStatus.INACTIVE);
        userService.updateUser(cmd);

        verify(tokenMetadataPort).revokeAllUserTokens(USER_ID.value());
    }

    @Test
    void listUsers_returnsAll() {
        when(userRepository.findAll()).thenReturn(List.of(
                User.builder().id(USER_ID).username("a").build(),
                User.builder().id(USER_ID_2).username("b").build()
        ));
        assertThat(userService.listUsers()).hasSize(2);
    }
}
