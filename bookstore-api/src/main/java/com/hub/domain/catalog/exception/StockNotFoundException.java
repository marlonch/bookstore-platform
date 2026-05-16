package com.hub.domain.catalog.exception;

import com.hub.domain.DomainException;

public class StockNotFoundException extends DomainException {
    public StockNotFoundException(String message) {
        super(message);
    }
}
