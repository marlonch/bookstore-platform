package com.hub.domain.catalog.exception;

import com.hub.domain.DomainException;

public class InvalidBookException extends DomainException {
    public InvalidBookException(String message) {
        super(message);
    }
}
