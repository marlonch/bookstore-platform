package com.hub.application.catalog.port.out;

import com.hub.domain.catalog.Book;

import java.util.List;
import java.util.Optional;


/**
 * Outbound port for persisting and retrieving {@link Book} domain objects.
 * <p>
 * This contract abstracts the underlying persistence mechanism used by the
 * application layer to manage books.
 */
public interface BookRepositoryPort {

    Book save(Book book);

    Optional<Book> findById(Long id);

    List<Book> findAll();

    List<Book> findByOwnerId(Long ownerId);

    void deleteById(Long id);

    boolean existsById(Long id);
}
