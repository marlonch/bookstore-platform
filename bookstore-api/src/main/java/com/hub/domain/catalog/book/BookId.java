package com.hub.domain.catalog.book;

import com.github.f4b6a3.uuid.UuidCreator;
import com.hub.domain.catalog.exception.InvalidBookException;

import java.util.UUID;

public record BookId(UUID value) {

    public BookId {
        if (value == null) {
            throw new InvalidBookException("BookId must not be null");
        }
    }

    public static BookId generate() {
        return new BookId(UuidCreator.getTimeOrderedEpoch());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
