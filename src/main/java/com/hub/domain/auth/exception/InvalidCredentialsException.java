package com.hub.domain.auth.exception;

import com.hub.domain.DomainException;

public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
