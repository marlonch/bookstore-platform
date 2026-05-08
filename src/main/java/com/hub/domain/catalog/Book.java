package com.hub.domain.catalog;

import com.hub.domain.catalog.exception.InvalidBookException;
import lombok.Getter;
import lombok.Setter;

/**
 * Core domain aggregate representing a book.
 * The ownerId remains null until the book is assigned to a user.
 */
@Getter
public class Book {

    private final Long id;
    private final String title;
    private final String author;
    private final Integer publishedYear;
    private Long ownerId;

    private Book(Long id, String title, String author, Integer publishedYear, Long ownerId) {
        validate(title, author, publishedYear);
        this.id = id;
        this.title = title;
        this.author = author;
        this.publishedYear = publishedYear;
        this.ownerId = ownerId;
    }

    /**
     * Creates a new transient book that has not yet been persisted.
     */
    public static Book createNew(String title, String author, Integer publishedYear) {
        return new Book(null, title, author, publishedYear, null);
    }

    /**
     * Reconstitutes an existing persisted book aggregate.
     */
    public static Book existing(Long id, String title, String author, Integer publishedYear, Long ownerId) {
        if (id == null) {
            throw new InvalidBookException("Book id must not be null");
        }
        return new Book(id, title, author, publishedYear, ownerId);
    }

    private static void validate(String title, String author, Integer publishedYear) {
        if (title == null || title.isBlank()) {
            throw new InvalidBookException("Title must not be empty");
        }
        if (author == null || author.isBlank()) {
            throw new InvalidBookException("Author must not be empty");
        }
        if (publishedYear == null || publishedYear < 1400) {
            throw new InvalidBookException("Published year is invalid");
        }
    }

    public void assignToOwner(Long ownerId) {
        if (ownerId == null) {
            throw new InvalidBookException("Owner id must not be null");
        }
        this.ownerId = ownerId;
    }

    public void unassignOwner() {
        this.ownerId = null;
    }
}
