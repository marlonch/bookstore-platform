package com.hub.adapters.in.rest.mapper;

import com.hub.adapters.in.rest.dto.request.CreateUserRequest;
import com.hub.adapters.in.rest.dto.request.UpdateUserRequest;
import com.hub.adapters.in.rest.dto.response.UserResponse;
import com.hub.application.identity.port.in.command.CreateUserCommand;
import com.hub.application.identity.port.in.command.UpdateUserCommand;
import com.hub.domain.identity.Role;
import com.hub.domain.identity.User;
import com.hub.domain.identity.UserId;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class UserRestMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId().value(), user.getUsername(), user.getEmail(),
                user.getRoles(), user.getStatus());
    }

    public CreateUserCommand toCreateCommand(CreateUserRequest request) {
        Set<Role> roles = (request.roles() != null && !request.roles().isEmpty())
                ? request.roles() : Set.of(Role.NON_ADMINISTRATOR);
        return new CreateUserCommand(request.username(), request.email(), request.password(), roles);
    }

    public UpdateUserCommand toUpdateCommand(UUID id, UpdateUserRequest request) {
        return new UpdateUserCommand(new UserId(id), request.username(), request.email(),
                request.password(), request.roles(), request.status());
    }
}
