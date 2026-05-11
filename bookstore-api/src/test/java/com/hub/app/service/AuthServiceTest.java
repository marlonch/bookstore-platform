package com.hub.app.service;

import com.hub.application.auth.port.in.command.LoginCommand;
import com.hub.application.auth.port.in.command.TokenGenerationCommand;
import com.hub.application.auth.port.in.result.LoginResult;
import com.hub.application.auth.port.out.PasswordHasherPort;
import com.hub.application.auth.port.out.TokenGeneratorPort;
import com.hub.application.auth.port.out.TokenMetadataRepositoryPort;
import com.hub.application.auth.service.AuthService;

import com.hub.application.identity.port.out.UserRepositoryPort;
import com.hub.domain.auth.TokenMetadata;
import com.hub.domain.auth.TokenStatus;
import com.hub.domain.auth.exception.InactiveUserException;
import com.hub.domain.auth.exception.InvalidCredentialsException;
import com.hub.domain.identity.Role;
import com.hub.domain.identity.User;
import com.hub.domain.identity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepositoryPort userRepository;
    @Mock
    TokenMetadataRepositoryPort tokenMetadataPort;
    @Mock
    PasswordHasherPort passwordHasher;
    @Mock
    TokenGeneratorPort tokenGenerator;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, tokenMetadataPort, passwordHasher, tokenGenerator, 24L);
    }

    @Test
    void login_withValidCredentials_savesMetadataAndReturnsToken() {
        User user = User.builder()
                .id(1L).username("alice").email("alice@test.com")
                .passwordHash("hashed").roles(Set.of(Role.ADMINISTRATOR))
                .status(UserStatus.ACTIVE).build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("pass", "hashed")).thenReturn(true);
        when(tokenGenerator.generate(any(TokenGenerationCommand.class))).thenReturn("jwt-token");

        LoginResult result = authService.login(new LoginCommand("alice", "pass"));

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.roles()).contains(Role.ADMINISTRATOR);

        ArgumentCaptor<TokenMetadata> captor = ArgumentCaptor.forClass(TokenMetadata.class);
        verify(tokenMetadataPort).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TokenStatus.ACTIVE);
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentialsException() {
        User user = User.builder()
                .id(1L).username("alice").passwordHash("hashed")
                .status(UserStatus.ACTIVE).build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand("alice", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_withUnknownUsername_throwsInvalidCredentialsException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginCommand("ghost", "pass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_withInactiveUser_throwsInactiveUserException() {
        User user = User.builder()
                .id(1L).username("alice").passwordHash("hashed")
                .status(UserStatus.INACTIVE).build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginCommand("alice", "pass")))
                .isInstanceOf(InactiveUserException.class);
    }

    @Test
    void login_withNullCommand_throwsNullPointerException() {
        assertThatThrownBy(() -> authService.login(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void login_tokenTtlMatchesConfiguredExpirationHours() {
        AuthService serviceWith2Hours = new AuthService(
                userRepository, tokenMetadataPort, passwordHasher, tokenGenerator, 2L);

        User user = User.builder()
                .id(1L).username("alice").email("alice@test.com")
                .passwordHash("hashed").roles(Set.of(Role.ADMINISTRATOR))
                .status(UserStatus.ACTIVE).build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("pass", "hashed")).thenReturn(true);
        when(tokenGenerator.generate(any(TokenGenerationCommand.class))).thenReturn("jwt-token");

        serviceWith2Hours.login(new LoginCommand("alice", "pass"));

        ArgumentCaptor<TokenMetadata> captor = ArgumentCaptor.forClass(TokenMetadata.class);
        verify(tokenMetadataPort).save(captor.capture());

        TokenMetadata saved = captor.getValue();
        long actualSeconds = Duration.between(saved.getIssuedAt(), saved.getExpiresAt()).getSeconds();
        assertThat(actualSeconds).isEqualTo(Duration.ofHours(2).getSeconds());
    }

    @Test
    void logout_revokesTokenById() {
        authService.logout("token-123");
        verify(tokenMetadataPort).revokeToken("token-123");
    }

    @Test
    void logout_withNullTokenId_throwsNullPointerException() {
        assertThatThrownBy(() -> authService.logout(null))
                .isInstanceOf(NullPointerException.class);
    }
}
