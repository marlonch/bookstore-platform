package com.hub.application.catalog.book.port.in;

import com.hub.application.catalog.book.port.in.command.UpdateBookCommand;
import com.hub.domain.catalog.book.Book;

public interface UpdateBookUseCase {
    Book updateBook(UpdateBookCommand command);
}
