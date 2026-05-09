package com.hub.domain.identity.exception;

import com.hub.domain.DomainException;

public class DuplicateEmailException extends DomainException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
