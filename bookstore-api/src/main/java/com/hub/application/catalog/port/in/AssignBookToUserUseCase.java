package com.hub.application.catalog.port.in;

import com.hub.application.catalog.port.in.command.AssignBookCommand;
import com.hub.domain.catalog.Book;

public interface AssignBookToUserUseCase {
    Book assignBookToUser(AssignBookCommand command);
}
