package com.hub.domain.auth.exception;

import com.hub.domain.DomainException;

public class InactiveUserException extends DomainException {
    public InactiveUserException(String message) {
        super(message);
    }
}
