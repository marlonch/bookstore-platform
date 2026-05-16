package com.hub.application.catalog.book.port.in;

import com.hub.domain.catalog.book.Book;

import java.util.List;

public interface ListBooksUseCase {
    List<Book> listBooks();
}
