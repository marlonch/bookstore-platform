package com.hub.bookstoreorderservice.domain.exception;

public class InvalidQuantityException extends DomainException {
    public InvalidQuantityException(String message) {
        super(message);
    }
}
