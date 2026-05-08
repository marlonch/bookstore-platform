package com.hub.application.identity.port.in;

import com.hub.domain.identity.User;

import java.util.List;

public interface ListUsersUseCase {
    List<User> listUsers();
}
