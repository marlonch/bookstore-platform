package com.hub.domain.catalog.book;

import com.github.f4b6a3.uuid.UuidCreator;
import com.hub.domain.catalog.exception.InvalidBookException;

import java.util.Objects;
import java.util.UUID;

public final class BookId {

    private final UUID value;

    public BookId(UUID value) {
        if (value == null) {
            throw new InvalidBookException("BookId must not be null");
        }
        this.value = value;
    }

    public static BookId generate() {
        return new BookId(UuidCreator.getTimeOrderedEpoch());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookId)) return false;
        return value.equals(((BookId) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}