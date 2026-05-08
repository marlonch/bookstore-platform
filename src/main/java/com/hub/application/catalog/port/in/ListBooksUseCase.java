package com.hub.application.catalog.port.in;

import com.hub.domain.catalog.Book;

import java.util.List;

public interface ListBooksUseCase {
    List<Book> listBooks();
}
