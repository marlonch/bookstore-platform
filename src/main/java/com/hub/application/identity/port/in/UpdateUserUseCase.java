package com.hub.application.identity.port.in;

import com.hub.domain.identity.User;
import com.hub.application.identity.port.in.command.UpdateUserCommand;

public interface UpdateUserUseCase {
    User updateUser(UpdateUserCommand command);
}
