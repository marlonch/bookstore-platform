package com.hub.adapters.out.persistence.jpa;

import com.hub.adapters.out.persistence.jpa.mapper.StockJpaMapper;
import com.hub.adapters.out.persistence.jpa.repository.StockJpaRepository;
import com.hub.application.catalog.stock.port.out.StockRepositoryPort;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StockJpaAdapter implements StockRepositoryPort {

    private final StockJpaRepository jpaRepository;
    private final StockJpaMapper mapper;

    @Override
    public Stock save(Stock stock) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(stock)));
    }

    @Override
    public Optional<Stock> findByBookId(BookId bookId) {
        return jpaRepository.findById(bookId.value()).map(mapper::toDomain);
    }
}
