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

    private static final String MSG_COMMAND_NULL = "command must not be null";
    private static final String MSG_STOCK_NOT_FOUND = "Stock not found for book: ";

    private final StockRepositoryPort stockRepository;
    private final TransactionPort transaction;

    @Override
    public Stock restock(RestockCommand command) {
        Objects.requireNonNull(command, MSG_COMMAND_NULL);
        return transaction.execute(() -> {
            Stock stock = stockRepository.findByBookIdForUpdate(command.bookId())
                    .orElseThrow(() -> new StockNotFoundException(MSG_STOCK_NOT_FOUND + command.bookId()));
            stock.restock(command.quantity());
            return stockRepository.save(stock);
        });
    }

    @Override
    public Stock reserve(ReserveStockCommand command) {
        Objects.requireNonNull(command, MSG_COMMAND_NULL);
        return transaction.execute(() -> {
            Stock stock = stockRepository.findByBookIdForUpdate(command.bookId())
                    .orElseThrow(() -> new StockNotFoundException(MSG_STOCK_NOT_FOUND + command.bookId()));
            stock.reserve(command.quantity());
            return stockRepository.save(stock);
        });
    }

    @Override
    public Stock release(ReleaseStockCommand command) {
        Objects.requireNonNull(command, MSG_COMMAND_NULL);
        return transaction.execute(() -> {
            Stock stock = stockRepository.findByBookIdForUpdate(command.bookId())
                    .orElseThrow(() -> new StockNotFoundException(MSG_STOCK_NOT_FOUND + command.bookId()));
            stock.release(command.quantity());
            return stockRepository.save(stock);
        });
    }
}
