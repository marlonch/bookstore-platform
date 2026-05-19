package com.hub.adapters.out.persistence.jpa.mapper;

import com.hub.adapters.out.persistence.jpa.entity.BookJpaEntity;
import com.hub.adapters.out.persistence.jpa.entity.UserJpaEntity;
import com.hub.domain.catalog.book.Book;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.book.BookStatus;
import com.hub.domain.catalog.book.ISBN;
import com.hub.domain.identity.UserId;
import org.springframework.stereotype.Component;

@Component
public class BookJpaMapper {

    public BookJpaEntity toEntity(Book book, UserJpaEntity owner) {
        return BookJpaEntity.builder()
                .id(book.getId().value())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publishedYear(book.getPublishedYear())
                .price(book.getPrice())
                .isbn(book.getIsbn().getValue())
                .status(book.getStatus().name())
                .owner(owner)
                .build();
    }

    public Book toDomain(BookJpaEntity entity) {
        ISBN isbn = new ISBN(entity.getIsbn());
        BookStatus status = entity.getStatus() != null
                ? BookStatus.valueOf(entity.getStatus())
                : BookStatus.ACTIVE;
        return Book.existing(
                new BookId(entity.getId()),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getPublishedYear(),
                entity.getPrice(),
                isbn,
                status,
                entity.getOwner() != null ? new UserId(entity.getOwner().getId()) : null);
    }
}
