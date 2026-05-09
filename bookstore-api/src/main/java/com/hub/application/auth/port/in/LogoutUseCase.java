package com.hub.application.auth.port.in;

public interface LogoutUseCase {
    void logout(String tokenId);
}
