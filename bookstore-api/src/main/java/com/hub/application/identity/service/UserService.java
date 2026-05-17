package com.hub.application.identity.service;

import com.hub.application.auth.port.out.PasswordHasherPort;
import com.hub.application.auth.port.out.TokenMetadataRepositoryPort;
import com.hub.application.identity.port.in.*;
import com.hub.application.identity.port.in.command.CreateUserCommand;
import com.hub.application.identity.port.in.command.UpdateUserCommand;
import com.hub.application.identity.port.out.UserRepositoryPort;
import com.hub.domain.auth.exception.UserNotFoundException;
import com.hub.domain.identity.Role;
import com.hub.domain.identity.User;
import com.hub.domain.identity.UserId;
import com.hub.domain.identity.UserStatus;
import com.hub.domain.identity.exception.DuplicateEmailException;
import com.hub.domain.identity.exception.DuplicateUsernameException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements CreateUserUseCase, GetUserUseCase, UpdateUserUseCase,
        DeleteUserUseCase, ListUsersUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenMetadataRepositoryPort tokenMetadataPort;

    @Override
    public User createUser(CreateUserCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException("Email already in use: " + command.email());
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new DuplicateUsernameException("Username already in use: " + command.username());
        }

        Set<Role> roles = (command.roles() != null && !command.roles().isEmpty())
                ? command.roles() : Set.of(Role.NON_ADMINISTRATOR);

        return userRepository.save(User.builder()
                .id(UserId.generate())
                .username(command.username())
                .email(command.email())
                .passwordHash(passwordHasher.encode(command.rawPassword()))
                .roles(roles)
                .status(UserStatus.ACTIVE)
                .build());
    }

    @Override
    public User getUser(UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    @Override
    public User updateUser(UpdateUserCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        User existing = userRepository.findById(command.id())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + command.id()));

        if (command.username() != null && !command.username().equals(existing.getUsername())
                && userRepository.existsByUsername(command.username())) {
            throw new DuplicateUsernameException("Username already in use: " + command.username());
        }
        if (command.email() != null && !command.email().equals(existing.getEmail())
                && userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException("Email already in use: " + command.email());
        }

        String newHash = (command.rawPassword() != null)
                ? passwordHasher.encode(command.rawPassword())
                : existing.getPasswordHash();

        UserStatus newStatus = (command.status() != null) ? command.status() : existing.getStatus();

        if (newStatus == UserStatus.INACTIVE || newStatus == UserStatus.BANNED) {
            tokenMetadataPort.revokeAllUserTokens(command.id().value());
        }

        return userRepository.save(User.builder()
                .id(command.id())
                .username(command.username() != null ? command.username() : existing.getUsername())
                .email(command.email() != null ? command.email() : existing.getEmail())
                .passwordHash(newHash)
                .roles(command.roles() != null ? command.roles() : existing.getRoles())
                .status(newStatus)
                .build());
    }

    @Override
    public void deleteUser(UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        tokenMetadataPort.revokeAllUserTokens(userId.value());
        userRepository.deleteById(userId);
    }

    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }
}
