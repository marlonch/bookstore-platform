package com.hub.domain.catalog;

import com.hub.domain.catalog.exception.InvalidBookException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {
    @Test
    void shouldCreateBookWhenDataIsValid() {
        Book book = new Book(1L, "Domain-Driven Design", "Marlon Cardenas", 2026);

        assertEquals(1L, book.getId());
        assertEquals("Domain-Driven Design", book.getTitle());
        assertEquals("Marlon Cardenas", book.getAuthor());
        assertEquals(2026, book.getPublishedYear());
    }

    @Test
    void shouldThrowExceptionWhenTitleIsNull() {
        assertThrows(InvalidBookException.class,
                () -> new Book(1L, null, "Marlon Cardenas", 2026));
    }

    @Test
    void shouldThrowExceptionWhenTitleIsBlank() {
        assertThrows(InvalidBookException.class,
                () -> new Book(1L, "   ", "Marlon Cardenas", 2026));
    }

    @Test
    void shouldThrowExceptionWhenAuthorIsNull() {
        assertThrows(InvalidBookException.class,
                () -> new Book(1L, "Domain-Driven Design", null, 2026));
    }

    @Test
    void shouldThrowExceptionWhenAuthorIsBlank() {
        assertThrows(InvalidBookException.class,
                () -> new Book(1L, "Domain-Driven Design", "   ", 2026));
    }

    @Test
    void shouldThrowExceptionWhenPublishedYearIsTooEarly() {
        assertThrows(InvalidBookException.class,
                () -> new Book(1L, "Some Book", "Some Author", 1200));
    }
}