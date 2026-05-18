package com.hub.application.catalog.stock.port.in.command;

import com.hub.domain.catalog.book.BookId;

public record UpdateStockCommand(BookId bookId, int quantity) {
}
