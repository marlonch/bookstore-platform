package com.hub.domain.identity.exception;

import com.hub.domain.DomainException;

public class DuplicateUsernameException extends DomainException {
    public DuplicateUsernameException(String message) {
        super(message);
    }
}
