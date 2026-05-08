package com.hub.application.identity.port.in;

import com.hub.domain.identity.User;

public interface GetUserUseCase {
    User getUser(Long userId);
}
