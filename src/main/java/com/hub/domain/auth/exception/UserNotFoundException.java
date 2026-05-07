package com.hub.domain.auth.exception;

import com.hub.domain.DomainException;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
