package com.hub.domain.catalog;

import com.hub.domain.catalog.book.ISBN;
import com.hub.domain.catalog.exception.InvalidBookException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ISBNTest {

    @Test
    void constructor_withValid13Digits_storesNormalizedValue() {
        ISBN isbn = new ISBN("9780134190440");
        assertThat(isbn.getValue()).isEqualTo("9780134190440");
    }

    @Test
    void constructor_withHyphens_normalizesAndAccepts() {
        ISBN isbn = new ISBN("978-0-13-419044-0");
        assertThat(isbn.getValue()).isEqualTo("9780134190440");
    }

    @Test
    void constructor_withSpaces_normalizesAndAccepts() {
        ISBN isbn = new ISBN("978 0 13 419044 0");
        assertThat(isbn.getValue()).isEqualTo("9780134190440");
    }

    @Test
    void constructor_withNull_throwsInvalidBookException() {
        assertThatThrownBy(() -> new ISBN(null))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void constructor_with12Digits_throwsInvalidBookException() {
        assertThatThrownBy(() -> new ISBN("978013419044"))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("13 digits");
    }

    @Test
    void constructor_with14Digits_throwsInvalidBookException() {
        assertThatThrownBy(() -> new ISBN("97801341904400"))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("13 digits");
    }

    @Test
    void constructor_withLetters_throwsInvalidBookException() {
        assertThatThrownBy(() -> new ISBN("978013419044X"))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void equals_sameValue_returnsTrue() {
        ISBN a = new ISBN("9780134190440");
        ISBN b = new ISBN("9780134190440");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void equals_differentValue_returnsFalse() {
        ISBN a = new ISBN("9780134190440");
        ISBN b = new ISBN("9780201633610");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void hashCode_sameValue_equal() {
        ISBN a = new ISBN("9780134190440");
        ISBN b = new ISBN("9780134190440");
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void toString_returnsNormalizedValue() {
        ISBN isbn = new ISBN("978-0-13-419044-0");
        assertThat(isbn.toString()).isEqualTo("9780134190440");
    }
}
