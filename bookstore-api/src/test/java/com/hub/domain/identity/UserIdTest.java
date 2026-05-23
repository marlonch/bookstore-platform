package com.hub.domain.identity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserIdTest {

    @Test
    void constructor_withValidUuid_storesValue() {
        UUID uuid = UUID.randomUUID();
        assertThat(new UserId(uuid).value()).isEqualTo(uuid);
    }

    @Test
    void constructor_withNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new UserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void generate_returnsNonNullId() {
        UserId userId = UserId.generate();
        assertThat(userId).isNotNull();
        assertThat(userId.value()).isNotNull();
    }

    @Test
    void generate_returnsDifferentValuesEachCall() {
        assertThat(UserId.generate()).isNotEqualTo(UserId.generate());
    }

    @Test
    void equals_sameUuid_returnsTrue() {
        UUID uuid = UUID.randomUUID();
        assertThat(new UserId(uuid)).isEqualTo(new UserId(uuid));
    }

    @Test
    void equals_differentUuid_returnsFalse() {
        assertThat(new UserId(UUID.randomUUID())).isNotEqualTo(new UserId(UUID.randomUUID()));
    }

    @Test
    void hashCode_sameUuid_isEqual() {
        UUID uuid = UUID.randomUUID();
        assertThat(new UserId(uuid)).hasSameHashCodeAs(new UserId(uuid));
    }

    @Test
    void toString_returnsPlainUuidString() {
        UUID uuid = UUID.randomUUID();
        assertThat(new UserId(uuid)).hasToString(uuid.toString());
    }
}