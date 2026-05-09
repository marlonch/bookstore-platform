package com.hub.domain.catalog;

import com.hub.domain.catalog.exception.InvalidBookException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {
    @Test
    void shouldCreateBookWhenDataIsValid() {
        Book book = Book.createNew( "Domain-Driven Design", "Marlon Cardenas", 2026);

        assertEquals("Domain-Driven Design", book.getTitle());
        assertEquals("Marlon Cardenas", book.getAuthor());
        assertEquals(2026, book.getPublishedYear());
    }

    @Test
    void shouldThrowExceptionWhenTitleIsNull() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew(null, "Marlon Cardenas", 2026));
    }

    @Test
    void shouldThrowExceptionWhenTitleIsBlank() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("   ", "Marlon Cardenas", 2026));
    }

    @Test
    void shouldThrowExceptionWhenAuthorIsNull() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("Domain-Driven Design", null, 2026));
    }

    @Test
    void shouldThrowExceptionWhenAuthorIsBlank() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew( "Domain-Driven Design", "   ", 2026));
    }

    @Test
    void shouldThrowExceptionWhenPublishedYearIsTooEarly() {
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("Some Book", "Some Author", 1200));
    }

    @Test
    void shouldThrowExceptionWhenPublishedYearIsInFuture() {
        int futureYear = java.time.Year.now().getValue() + 1;
        assertThrows(InvalidBookException.class,
                () -> Book.createNew("Future Book", "Some Author", futureYear));
    }
}