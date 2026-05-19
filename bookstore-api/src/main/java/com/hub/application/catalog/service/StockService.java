package com.hub.application.catalog.service;

import com.hub.application.catalog.stock.port.in.ReleaseStockUseCase;
import com.hub.application.catalog.stock.port.in.ReserveStockUseCase;
import com.hub.application.catalog.stock.port.in.RestockUseCase;
import com.hub.application.catalog.stock.port.in.command.ReleaseStockCommand;
import com.hub.application.catalog.stock.port.in.command.ReserveStockCommand;
import com.hub.application.catalog.stock.port.in.command.RestockCommand;
import com.hub.application.catalog.stock.port.out.StockRepositoryPort;
import com.hub.application.shared.port.out.TransactionPort;
import com.hub.domain.catalog.exception.StockNotFoundException;
import com.hub.domain.catalog.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StockService implements RestockUseCase, ReserveStockUseCase, ReleaseStockUseCase {

    private final StockRepositoryPort stockRepository;
    private final TransactionPort transaction;

    @Override
    public Stock restock(RestockCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        return transaction.execute(() -> {
            Stock stock = stockRepository.findByBookIdForUpdate(command.bookId())
                    .orElseThrow(() -> new StockNotFoundException("Stock not found for book: " + command.bookId()));
            stock.restock(command.quantity());
            return stockRepository.save(stock);
        });
    }

    @Override
    public Stock reserve(ReserveStockCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        return transaction.execute(() -> {
            Stock stock = stockRepository.findByBookIdForUpdate(command.bookId())
                    .orElseThrow(() -> new StockNotFoundException("Stock not found for book: " + command.bookId()));
            stock.reserve(command.quantity());
            return stockRepository.save(stock);
        });
    }

    @Override
    public Stock release(ReleaseStockCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        return transaction.execute(() -> {
            Stock stock = stockRepository.findByBookIdForUpdate(command.bookId())
                    .orElseThrow(() -> new StockNotFoundException("Stock not found for book: " + command.bookId()));
            stock.release(command.quantity());
            return stockRepository.save(stock);
        });
    }
}
