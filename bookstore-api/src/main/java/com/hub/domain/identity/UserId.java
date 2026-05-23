package com.hub.domain.identity;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
    }

    public static UserId generate() {
        return new UserId(UuidCreator.getTimeOrderedEpoch());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}