package com.hub.application.catalog.stock.port.in.command;

import com.hub.domain.catalog.book.BookId;

public record RestockCommand(BookId bookId, int quantity) {}
