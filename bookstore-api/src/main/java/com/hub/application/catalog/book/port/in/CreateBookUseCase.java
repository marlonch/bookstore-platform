package com.hub.application.catalog.book.port.in;

import com.hub.application.catalog.book.port.in.command.CreateBookCommand;
import com.hub.domain.catalog.book.Book;

public interface CreateBookUseCase {
    Book createBook(CreateBookCommand command);
}
