package com.hub.domain.catalog.exception;

import com.hub.domain.DomainException;

public class DuplicateIsbnException extends DomainException {
    public DuplicateIsbnException(String message) {
        super(message);
    }
}
