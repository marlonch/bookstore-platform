package com.hub.application.catalog.book.port.in;

import com.hub.application.catalog.book.port.in.command.AssignBookCommand;
import com.hub.domain.catalog.book.Book;

public interface AssignBookToUserUseCase {
    Book assignBookToUser(AssignBookCommand command);
}
