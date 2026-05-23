package com.hub.domain.catalog;

import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.exception.InvalidBookException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookIdTest {

    @Test
    void constructor_withValidUuid_storesValue() {
        UUID uuid = UUID.randomUUID();
        assertThat(new BookId(uuid).value()).isEqualTo(uuid);
    }

    @Test
    void constructor_withNull_throwsInvalidBookException() {
        assertThatThrownBy(() -> new BookId(null))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void generate_returnsNonNullId() {
        BookId bookId = BookId.generate();
        assertThat(bookId).isNotNull();
        assertThat(bookId.value()).isNotNull();
    }

    @Test
    void generate_returnsDifferentValuesEachCall() {
        assertThat(BookId.generate()).isNotEqualTo(BookId.generate());
    }

    @Test
    void equals_sameUuid_returnsTrue() {
        UUID uuid = UUID.randomUUID();
        assertThat(new BookId(uuid)).isEqualTo(new BookId(uuid));
    }

    @Test
    void equals_differentUuid_returnsFalse() {
        assertThat(new BookId(UUID.randomUUID())).isNotEqualTo(new BookId(UUID.randomUUID()));
    }

    @Test
    void hashCode_sameUuid_isEqual() {
        UUID uuid = UUID.randomUUID();
        assertThat(new BookId(uuid).hashCode()).isEqualTo(new BookId(uuid).hashCode());
    }

    @Test
    void toString_returnsPlainUuidString() {
        UUID uuid = UUID.randomUUID();
        assertThat(new BookId(uuid).toString()).isEqualTo(uuid.toString());
    }
}