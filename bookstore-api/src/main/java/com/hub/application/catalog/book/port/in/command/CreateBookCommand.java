package com.hub.application.catalog.book.port.in.command;

import com.hub.domain.catalog.book.ISBN;

import java.math.BigDecimal;

public record CreateBookCommand(
        String title,
        String author,
        Integer publishedYear,
        BigDecimal price,
        ISBN isbn,
        int initialStock
) {
    public CreateBookCommand {
        if (initialStock < 1) initialStock = 1;
    }
}
