package com.hub.domain.catalog;

import com.hub.domain.catalog.exception.InvalidBookException;
import lombok.Getter;

/** Core domain book. ownerId is null until a book is assigned to a user. */
@Getter
public class Book {

    private final Long id;
    private final String title;
    private final String author;
    private final Integer publishedYear;
    private Long ownerId;

    public Book(Long id, String title, String author, Integer publishedYear) {
        if (title == null || title.isBlank()) {
            throw new InvalidBookException("Title must not be empty");
        }
        if (author == null || author.isBlank()) {
            throw new InvalidBookException("Author must not be empty");
        }
        if (publishedYear == null || publishedYear < 1400) {
            throw new InvalidBookException("Published year is invalid");
        }
        this.id = id;
        this.title = title;
        this.author = author;
        this.publishedYear = publishedYear;
        this.ownerId = null;
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
