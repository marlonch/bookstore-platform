package com.hub.application.auth.port.in;

import com.hub.application.auth.port.in.command.LoginCommand;
import com.hub.application.auth.port.in.result.LoginResult;

/**
 * Use case for authenticating a user with their credentials.
 * <p>
 * This inbound port is invoked by input adapters (e.g. REST controllers)
 * to perform the login flow.
 */
public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
