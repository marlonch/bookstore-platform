package com.hub.application.catalog.port.in;

import com.hub.domain.catalog.Book;

public interface GetBookUseCase {
    Book getBook(Long bookId);
}
