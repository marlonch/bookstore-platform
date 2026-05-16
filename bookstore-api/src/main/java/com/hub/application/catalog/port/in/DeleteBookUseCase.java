package com.hub.application.catalog.port.in;

import com.hub.domain.catalog.book.BookId;

public interface DeleteBookUseCase {
    void deleteBook(BookId bookId);
}