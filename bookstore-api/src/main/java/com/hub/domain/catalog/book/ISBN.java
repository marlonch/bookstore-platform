package com.hub.domain.catalog.book;

import com.hub.domain.catalog.exception.InvalidBookException;

import java.util.Objects;

public final class ISBN {

    private final String value;

    public ISBN(String raw) {
        if (raw == null) {
            throw new InvalidBookException("ISBN must not be null");
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() != 13) {
            throw new InvalidBookException("ISBN must be 13 digits");
        }
        this.value = digits;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ISBN)) return false;
        return value.equals(((ISBN) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
