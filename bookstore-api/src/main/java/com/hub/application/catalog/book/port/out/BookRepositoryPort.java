package com.hub.application.catalog.book.port.out;

import com.hub.domain.catalog.book.Book;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.identity.UserId;

import java.util.List;
import java.util.Optional;

public interface BookRepositoryPort {

    Book save(Book book);

    Optional<Book> findById(BookId id);

    List<Book> findAll();

    List<Book> findByOwnerId(UserId ownerId);

    void deleteById(BookId id);

    boolean existsById(BookId id);
}
