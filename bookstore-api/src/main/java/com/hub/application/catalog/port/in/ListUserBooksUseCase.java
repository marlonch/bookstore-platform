package com.hub.application.catalog.port.in;

import com.hub.domain.catalog.book.Book;
import com.hub.domain.identity.UserId;

import java.util.List;

public interface ListUserBooksUseCase {
    List<Book> listUserBooks(UserId userId);
}