package com.hub.domain.identity;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Objects;
import java.util.UUID;

public final class UserId {

    private final UUID value;

    public UserId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        this.value = value;
    }

    public static UserId generate() {
        return new UserId(UuidCreator.getTimeOrderedEpoch());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId)) return false;
        return value.equals(((UserId) o).value);
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