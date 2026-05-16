package com.hub.adapters.out.persistence.jpa.mapper;

import com.hub.adapters.out.persistence.jpa.entity.StockJpaEntity;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.stock.Stock;
import org.springframework.stereotype.Component;

@Component
public class StockJpaMapper {

    public StockJpaEntity toEntity(Stock stock) {
        StockJpaEntity entity = new StockJpaEntity();
        entity.setBookId(stock.getBookId().value());
        entity.setQuantity(stock.getAvailableQuantity());
        return entity;
    }

    public Stock toDomain(StockJpaEntity entity) {
        return Stock.existing(new BookId(entity.getBookId()), entity.getQuantity());
    }
}