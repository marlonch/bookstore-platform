package com.hub.domain;

/** Base class for all domain exceptions. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
