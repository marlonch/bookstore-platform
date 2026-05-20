package com.hub.adapters.in.rest.controller;

import com.hub.adapters.in.rest.dto.request.CreateUserRequest;
import com.hub.adapters.in.rest.dto.request.UpdateUserRequest;
import com.hub.adapters.in.rest.dto.response.BookResponse;
import com.hub.adapters.in.rest.dto.response.UserResponse;
import com.hub.adapters.in.rest.mapper.BookRestMapper;
import com.hub.adapters.in.rest.mapper.UserRestMapper;
import com.hub.adapters.security.UserDetailsImpl;
import com.hub.application.catalog.book.port.in.ListUserBooksUseCase;
import com.hub.application.identity.port.in.*;
import com.hub.domain.identity.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final ListUserBooksUseCase listUserBooksUseCase;
    private final UserRestMapper userMapper;
    private final BookRestMapper bookMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(listUsersUseCase.listUsers().stream()
                .map(userMapper::toResponse).toList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(createUserUseCase.createUser(userMapper.toCreateCommand(request))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userMapper.toResponse(getUserUseCase.getUser(new UserId(id))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userMapper.toResponse(
                updateUserUseCase.updateUser(userMapper.toUpdateCommand(id, request))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        deleteUserUseCase.deleteUser(new UserId(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/books")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookResponse>> getUserBooks(@PathVariable UUID id,
            Authentication authentication) {
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMINISTRATOR"));

        if (!isAdmin && !id.equals(principal.getId())) {
            throw new AccessDeniedException("Cannot access books of another user");
        }

        return ResponseEntity.ok(listUserBooksUseCase.listUserBooks(new UserId(id)).stream()
                .map(bookMapper::toResponse).toList());
    }
}
