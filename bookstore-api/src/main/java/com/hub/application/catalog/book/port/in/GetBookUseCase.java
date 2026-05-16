package com.hub.application.catalog.book.port.in;

import com.hub.domain.catalog.book.Book;
import com.hub.domain.catalog.book.BookId;

public interface GetBookUseCase {
    Book getBook(BookId bookId);
}