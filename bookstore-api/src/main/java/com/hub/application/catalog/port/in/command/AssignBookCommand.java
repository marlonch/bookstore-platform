package com.hub.application.catalog.port.in.command;

import com.hub.domain.catalog.book.BookId;
import com.hub.domain.identity.UserId;

public record AssignBookCommand(BookId bookId, UserId userId) {}
