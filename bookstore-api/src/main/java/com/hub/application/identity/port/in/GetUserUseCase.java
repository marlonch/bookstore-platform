package com.hub.application.identity.port.in;

import com.hub.domain.identity.User;
import com.hub.domain.identity.UserId;

public interface GetUserUseCase {
    User getUser(UserId userId);
}