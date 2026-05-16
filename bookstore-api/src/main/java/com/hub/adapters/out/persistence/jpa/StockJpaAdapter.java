package com.hub.adapters.out.persistence.jpa;

import com.hub.adapters.out.persistence.jpa.entity.BookJpaEntity;
import com.hub.adapters.out.persistence.jpa.entity.StockJpaEntity;
import com.hub.adapters.out.persistence.jpa.mapper.StockJpaMapper;
import com.hub.adapters.out.persistence.jpa.repository.BookJpaRepository;
import com.hub.adapters.out.persistence.jpa.repository.StockJpaRepository;
import com.hub.application.catalog.stock.port.out.StockRepositoryPort;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.stock.Stock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StockJpaAdapter implements StockRepositoryPort {

    private final StockJpaRepository jpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final StockJpaMapper mapper;

    @Override
    public Stock save(Stock stock) {
        UUID id = stock.getBookId().value();
        StockJpaEntity entity = jpaRepository.findById(id).orElseGet(() -> {
            BookJpaEntity bookRef = bookJpaRepository.getReferenceById(id);
            return mapper.toEntity(stock, bookRef);
        });
        entity.setQuantity(stock.getAvailableQuantity());
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Stock> findByBookId(BookId bookId) {
        return jpaRepository.findById(bookId.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Stock> findByBookIdForUpdate(BookId bookId) {
        return jpaRepository.findByBookIdForUpdate(bookId.value()).map(mapper::toDomain);
    }


}
