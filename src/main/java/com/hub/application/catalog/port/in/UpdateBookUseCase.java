package com.hub.application.catalog.port.in;

import com.hub.application.catalog.port.in.command.UpdateBookCommand;
import com.hub.domain.catalog.Book;

public interface UpdateBookUseCase {
    Book updateBook(UpdateBookCommand command);
}
