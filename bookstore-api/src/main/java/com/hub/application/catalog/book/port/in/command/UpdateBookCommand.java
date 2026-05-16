package com.hub.application.catalog.book.port.in.command;

import com.hub.domain.catalog.book.BookId;

import java.math.BigDecimal;

public record UpdateBookCommand(BookId id, String title, String author, Integer publishedYear, BigDecimal price) {}
