package com.hub.application.identity.port.in;

import com.hub.application.identity.port.in.command.CreateUserCommand;
import com.hub.domain.identity.User;

public interface CreateUserUseCase {
    User createUser(CreateUserCommand command);
}
