package com.hub.domain.catalog.stock;

import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.exception.InsufficientStockException;
import com.hub.domain.catalog.exception.InvalidBookException;
import lombok.Getter;

@Getter
public class Stock {

    private final BookId bookId;
    private int availableQuantity;

    private Stock(BookId bookId, int availableQuantity) {
        this.bookId = bookId;
        this.availableQuantity = availableQuantity;
    }

    public static Stock createNew(BookId bookId, int initialQuantity) {
        validate(bookId, initialQuantity);
        return new Stock(bookId, initialQuantity);
    }

    public static Stock existing(BookId bookId, int currentQuantity) {
        validate(bookId, currentQuantity);
        return new Stock(bookId, currentQuantity);
    }

    private static void validate(BookId bookId, int quantity) {
        if (bookId == null) {
            throw new InvalidBookException("bookId must not be null");
        }
        if (quantity < 0) {
            throw new InvalidBookException("Quantity must be >= 0");
        }
    }

    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidBookException("Restock quantity must be > 0");
        }
        this.availableQuantity += quantity;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new InvalidBookException("Reserve quantity must be > 0");
        }
        if (quantity > availableQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock: requested " + quantity + ", available " + availableQuantity);
        }
        this.availableQuantity -= quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new InvalidBookException("Release quantity must be > 0");
        }
        this.availableQuantity += quantity;
    }
}