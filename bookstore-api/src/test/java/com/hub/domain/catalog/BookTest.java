package com.hub.domain.catalog;

import com.hub.domain.catalog.book.ISBN;
import com.hub.domain.catalog.book.Book;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.book.BookStatus;
import com.hub.domain.catalog.exception.InvalidBookException;
import com.hub.domain.identity.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookTest {

    private static final BigDecimal VALID_PRICE = new BigDecimal("29.99");
    private static final ISBN VALID_ISBN = new ISBN("9780134190440");

    // --- createNew ---

    @Test
    void createNew_withValidData_setsFieldsAndActiveStatus() {
        Book book = Book.createNew("Domain-Driven Design", "Marlon Cardenas", 2026, VALID_PRICE, VALID_ISBN);

        assertThat(book.getTitle()).isEqualTo("Domain-Driven Design");
        assertThat(book.getAuthor()).isEqualTo("Marlon Cardenas");
        assertThat(book.getPublishedYear()).isEqualTo(2026);
        assertThat(book.getPrice()).isEqualByComparingTo(VALID_PRICE);
        assertThat(book.getIsbn()).isEqualTo(VALID_ISBN);
        assertThat(book.getStatus()).isEqualTo(BookStatus.ACTIVE);
        assertThat(book.getOwnerId()).isEmpty();
    }

    @Test
    void createNew_withNullTitle_throwsInvalidBookException() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew(null, "Author", 2020, VALID_PRICE, VALID_ISBN));
    }

    @Test
    void createNew_withBlankTitle_throwsInvalidBookException() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("   ", "Author", 2020, VALID_PRICE, VALID_ISBN));
    }

    @Test
    void createNew_withNullAuthor_throwsInvalidBookException() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("Title", null, 2020, VALID_PRICE, VALID_ISBN));
    }

    @Test
    void createNew_withBlankAuthor_throwsInvalidBookException() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("Title", "   ", 2020, VALID_PRICE, VALID_ISBN));
    }

    @Test
    void createNew_withYearTooEarly_throwsInvalidBookException() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("Title", "Author", 1200, VALID_PRICE, VALID_ISBN));
    }

    @Test
    void createNew_withFutureYear_throwsInvalidBookException() {
        int futureYear = java.time.Year.now().getValue() + 1;
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("Title", "Author", futureYear, VALID_PRICE, VALID_ISBN));
    }

    @Test
    void createNew_withZeroPrice_throwsInvalidBookException() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("Title", "Author", 2020, BigDecimal.ZERO, VALID_ISBN));
    }

    @Test
    void createNew_withNegativePrice_throwsInvalidBookException() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("Title", "Author", 2020, new BigDecimal("-1.00"), VALID_ISBN));
    }

    @Test
    void createNew_withNullIsbn_throwsInvalidBookException() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("Title", "Author", 2020, VALID_PRICE, null));
    }

    // --- existing ---

    @Test
    void existing_withNullId_throwsInvalidBookException() {
        assertThatThrownBy(() -> Book.existing(null, "Title", "Author", 2020, VALID_PRICE, VALID_ISBN, BookStatus.ACTIVE, null))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void existing_withOwnerId_getOwnerIdReturnsNonEmpty() {
        UserId ownerId = UserId.generate();
        Book book = Book.existing(BookId.generate(), "Title", "Author", 2020, VALID_PRICE, VALID_ISBN, BookStatus.ACTIVE, ownerId);

        assertThat(book.getOwnerId()).isPresent().contains(ownerId);
    }

    @Test
    void existing_withNullOwnerId_getOwnerIdReturnsEmpty() {
        Book book = Book.existing(BookId.generate(), "Title", "Author", 2020, VALID_PRICE, VALID_ISBN, BookStatus.ACTIVE, null);

        assertThat(book.getOwnerId()).isEmpty();
    }

    @Test
    void existing_withInactiveStatus_preservesStatus() {
        Book book = Book.existing(BookId.generate(), "Title", "Author", 2020, VALID_PRICE, VALID_ISBN, BookStatus.INACTIVE, null);

        assertThat(book.getStatus()).isEqualTo(BookStatus.INACTIVE);
    }

    // --- deactivate ---

    @Test
    void deactivate_fromActive_setsStatusToInactive() {
        Book book = Book.createNew("Title", "Author", 2020, VALID_PRICE, VALID_ISBN);

        book.deactivate();

        assertThat(book.getStatus()).isEqualTo(BookStatus.INACTIVE);
    }

    @Test
    void deactivate_whenAlreadyInactive_throwsInvalidBookException() {
        Book book = Book.createNew("Title", "Author", 2020, VALID_PRICE, VALID_ISBN);
        book.deactivate();

        assertThatThrownBy(book::deactivate)
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("already inactive");
    }
}
