package com.hub.domain.catalog.exception;

import com.hub.domain.DomainException;

public class BookNotFoundException extends DomainException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
