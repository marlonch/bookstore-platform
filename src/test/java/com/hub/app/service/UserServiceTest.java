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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepositoryPort userRepository;
    @Mock
    PasswordHasherPort passwordHasher;
    @Mock
    TokenMetadataRepositoryPort tokenMetadataPort;

    @InjectMocks
    UserService userService;

    @Test
    void createUser_withValidData_savesAndReturnsUser() {
        CreateUserCommand cmd = new CreateUserCommand("bob", "bob@test.com", "secret", Set.of(Role.NON_ADMINISTRATOR));

        when(userRepository.existsByEmail("bob@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(passwordHasher.encode("secret")).thenReturn("bcrypt-hash");

        User saved = User.builder().id(2L).username("bob").email("bob@test.com")
                .passwordHash("bcrypt-hash").roles(Set.of(Role.NON_ADMINISTRATOR))
                .status(UserStatus.ACTIVE).build();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.createUser(cmd);

        assertThat(result.getId()).isEqualTo(2L);
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
        User user = User.builder().id(1L).username("alice").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userService.getUser(1L).getUsername()).isEqualTo("alice");
    }

    @Test
    void getUser_withMissingId_throwsUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUser(99L)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteUser_withExistingId_revokesTokensAndDeletes() {
        when(userRepository.existsById(3L)).thenReturn(true);

        userService.deleteUser(3L);

        verify(tokenMetadataPort).revokeAllUserTokens(3L);
        verify(userRepository).deleteById(3L);
    }

    @Test
    void deleteUser_withMissingId_throwsUserNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUser(99L)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_disablingUser_revokesAllTokens() {
        User existing = User.builder().id(1L).username("alice").email("a@test.com")
                .passwordHash("old-hash").roles(Set.of(Role.NON_ADMINISTRATOR))
                .status(UserStatus.ACTIVE).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserCommand cmd = new UpdateUserCommand(1L, null, null, null, null, UserStatus.INACTIVE);
        userService.updateUser(cmd);

        verify(tokenMetadataPort).revokeAllUserTokens(1L);
    }

    @Test
    void listUsers_returnsAll() {
        when(userRepository.findAll()).thenReturn(List.of(
                User.builder().id(1L).username("a").build(),
                User.builder().id(2L).username("b").build()
        ));
        assertThat(userService.listUsers()).hasSize(2);
    }
}
