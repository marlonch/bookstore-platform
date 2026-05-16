package com.hub.application.catalog.stock.port.out;

import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.stock.Stock;

import java.util.Optional;

public interface StockRepositoryPort {
    Stock save(Stock stock);
    Optional<Stock> findByBookId(BookId bookId);
}
