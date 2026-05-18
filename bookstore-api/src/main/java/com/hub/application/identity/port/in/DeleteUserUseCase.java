package com.hub.application.identity.port.in;

import com.hub.domain.identity.UserId;

public interface DeleteUserUseCase {
    void deleteUser(UserId userId);
}
