package com.hub.domain.catalog.book;

import com.hub.domain.catalog.exception.InvalidBookException;
import com.hub.domain.identity.UserId;
import lombok.AccessLevel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Optional;

@Getter
public class Book {

    private final BookId id;
    private final String title;
    private final String author;
    private final Integer publishedYear;
    private final BigDecimal price;
    private final ISBN isbn;
    private BookStatus status;
    @Getter(AccessLevel.NONE)
    private final UserId ownerId;

    private Book(BookId id, String title, String author, Integer publishedYear,
                 BigDecimal price, ISBN isbn, BookStatus status, UserId ownerId) {
        validate(title, author, publishedYear, price, isbn);
        this.id = id;
        this.title = title;
        this.author = author;
        this.publishedYear = publishedYear;
        this.price = price;
        this.isbn = isbn;
        this.status = status;
        this.ownerId = ownerId;
    }

    public static Book createNew(String title, String author, Integer publishedYear,
                                 BigDecimal price, ISBN isbn) {
        return new Book(BookId.generate(), title, author, publishedYear, price, isbn, BookStatus.ACTIVE, null);
    }

    public static Book existing(BookId id, String title, String author, Integer publishedYear,
                                BigDecimal price, ISBN isbn, BookStatus status, UserId ownerId) {
        if (id == null) {
            throw new InvalidBookException("Book id must not be null");
        }
        return new Book(id, title, author, publishedYear, price, isbn, status, ownerId);
    }

    public void deactivate() {
        if (this.status == BookStatus.INACTIVE) {
            throw new InvalidBookException("Book is already inactive");
        }
        this.status = BookStatus.INACTIVE;
    }

    public Optional<UserId> getOwnerId() {
        return Optional.ofNullable(ownerId);
    }

    private static void validate(String title, String author, Integer publishedYear,
                                 BigDecimal price, ISBN isbn) {
        if (title == null || title.isBlank()) {
            throw new InvalidBookException("Title must not be empty");
        }
        if (author == null || author.isBlank()) {
            throw new InvalidBookException("Author must not be empty");
        }
        if (publishedYear == null || publishedYear < 1400) {
            throw new InvalidBookException("Published year is invalid");
        }
        if (publishedYear > Year.now().getValue()) {
            throw new InvalidBookException("Published year cannot be in the future");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBookException("Price must be greater than zero");
        }
        if (isbn == null) {
            throw new InvalidBookException("ISBN must not be null");
        }
    }
}