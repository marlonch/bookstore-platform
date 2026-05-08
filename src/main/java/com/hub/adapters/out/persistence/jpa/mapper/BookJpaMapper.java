package com.hub.adapters.out.persistence.jpa.mapper;

import com.hub.adapters.out.persistence.jpa.entity.BookJpaEntity;
import com.hub.adapters.out.persistence.jpa.entity.UserJpaEntity;
import com.hub.domain.catalog.Book;
import org.springframework.stereotype.Component;

/**
 * Maps domain {@link Book} aggregates to and from their JPA persistence
 * representation.
 *
 * <p>This mapper isolates persistence conversion logic from the domain model,
 * preventing persistence concerns from leaking into the core business layer.</p>
 */
@Component
public class BookJpaMapper {

    /**
     * Maps a domain {@link Book} aggregate into its JPA persistence representation.
     *
     * <p>The owner association is optional because books may exist without
     * an assigned owner.</p>
     *
     *  @param book the domain book to convert
     *  @param owner the JPA owner entity associated with the book, or {@code null} if the book has no owner
     *  @return the JPA entity representation of the given book
     */
    public BookJpaEntity toEntity(Book book, UserJpaEntity owner) {
        return BookJpaEntity.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publishedYear(book.getPublishedYear())
                .owner(owner)
                .build();
    }

    /**
     * Maps a JPA {@link BookJpaEntity} into the domain {@link Book} aggregate.
     *
     * <p>If the persistence entity has an associated owner, the owner's identifier
     * is propagated to the domain model; otherwise, the owner identifier is
     * {@code null}.</p>
     *
     * @param entity the persistence entity to convert
     * @return the domain aggregate created from the persistence entity
     */
    public Book toDomain(BookJpaEntity entity) {
        return Book.existing(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getPublishedYear(),
                entity.getOwner() != null ? entity.getOwner().getId() : null);

    }
}
