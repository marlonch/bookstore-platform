package com.hub.bookstoreorderservice.domain.exception;

public class InvalidOrderException extends DomainException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
