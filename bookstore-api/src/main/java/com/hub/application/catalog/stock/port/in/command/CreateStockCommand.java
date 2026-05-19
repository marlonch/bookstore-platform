package com.hub.application.catalog.stock.port.in.command;

import com.hub.domain.catalog.book.BookId;

public record CreateStockCommand(BookId bookId, int initialQuantity) {
}
